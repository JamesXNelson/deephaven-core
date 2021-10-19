package io.deephaven.demo;

import io.deephaven.demo.deploy.*;
import io.smallrye.common.constraint.NotNull;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.mutiny.core.Vertx;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jboss.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import static io.deephaven.demo.NameConstants.*;
import static io.deephaven.demo.deploy.GoogleDeploymentManager.*;

/**
 * DomainController:
 * <p>
 * <p> The brains enforcing our cluster management policies via live machine-to-dns-to-ip mappings.
 * <p>
 * <p> This class reads in data from google, and uses that to glue together a set of IP addresses {@link IpPool},
 * <p> to DNS records {@link DnsPool}.
 * <p>
 * Created by James X. Nelson (James@WeTheInter.net) on 25/09/2021 @ 2:25 a.m..
 */
public class ClusterController {

    private static final Logger LOG = Logger.getLogger(ClusterController.class);

    private final GoogleDeploymentManager manager;
    private final IpPool ips;
    private final DomainPool domains;
    private final MachinePool machines;
    private final CountDownLatch latch;
    private final OkHttpClient client;
    private static ZoneId TZ_NY = ZoneOffset.of("-6");
    private static LocalTime BIZ_START = LocalTime.of(6, 0);
    // We are using NY timezone, but want to cover all N.A. business hours, so our end is 9pm NY
    private static LocalTime BIZ_END = LocalTime.of(21, 0);

    public ClusterController() {
        this(new GoogleDeploymentManager("/tmp"));
    }
    public ClusterController(@NotNull final GoogleDeploymentManager manager) {
        this(manager, true);
    }
    public ClusterController(@NotNull final GoogleDeploymentManager manager, boolean loadMachines) {
        this.manager = manager;
        this.client = new OkHttpClient();
        latch = new CountDownLatch(loadMachines ? 4 : 2);
        if (manager == null) {
            throw new NullPointerException("Manager cannot be null");
        }
        this.ips = new IpPool();
        this.domains = new DomainPool();
        this.machines = new MachinePool();

        // always load IPs... we want them in cases when we manually create named machines
        setTimer("Load Unused IPs", this::loadIpsUnused);
        setTimer("Load Used IPs", this::loadIpsUsed);
        if (loadMachines) {
            // don't load machines or start any other controller threads if we are manually creating machines (ImageDeployer)
            setTimer("Load Machines", this::loadMachines);
            setTimer("Load Domains", this::loadDomains);
            setTimer("Wait until loaded", ()->{
                waitUntilReady();
                // a little extra delay: give user a chance to request a machine before we possibly clean it up!
                setTimer("Refresh state", ()-> {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                    checkState();
                    monitorLoop();
                });
            });
        }
    }

    public static void setTimer(String name, Callable<?> r) {
        setTimer(name, ()-> {
            try {
                r.call();
            } catch (Exception e) {
                LOG.error("Error on thread " + name, e);
            }
        });
    }
    public static void setTimer(String name, Runnable r) {
        new Thread(name) {
            @Override
            public void run() {
                try {
                    r.run();
                } catch (Throwable t) {
                    LOG.error("Unexpected error occurred running " + name, t);
                    throw t;
                }
            }
            {
                setDaemon(true);
            }
        }.start();
    }

    private void monitorLoop() {
        final Vertx vx = Vertx.vertx();
        vx.setTimer(TimeUnit.MINUTES.toMillis(1), t->{
            setTimer("Monitor Loop", ()->{
                try {
                    checkState();
                } finally {
                    monitorLoop();
                }
            });
        });
    }

    private synchronized void checkState() {
        // check for machines that have exceeded their limit, reboot them, and then put them back in the usable pool
        Set<Machine> runningMachines = new ConcurrentHashSet<>();
        Set<Machine> availableMachines = new ConcurrentHashSet<>();
        Set<Machine> offlineMachines = new ConcurrentHashSet<>();
        Set<Machine> usedMachines = new ConcurrentHashSet<>();
        machines.getAllMachines().forEach(machine -> {
            if (machine.isOnline()) {
                runningMachines.add(machine);
                if (machine.isInUse()) {
                    usedMachines.add(machine);
                } else {
                    availableMachines.add(machine);
                }
            } else {
                machine.setInUse(false);
                offlineMachines.add(machine);
            }
            if (machine.isInUse()) {
                if (machine.getExpiry() > 0 && machine.getExpiry() < System.currentTimeMillis()) {
                    // machine is past expiry... lets turn this box off, unless it's version is old, in which case, delete it

                    turnOff(machine);
                }
            }
        });
        int numRunning = runningMachines.size();
        int poolSize = getPoolSize();
        int numBuffer = getPoolBuffer();
        LOG.info("Done checking state:\n" +
                runningMachines.size() + " running machines\n" +
                offlineMachines.size() + " offline machines\n" +
                usedMachines.size() + " used machines\n" +
                availableMachines.size() + " available machines\n" +
                poolSize + " or more running machines desired\n" +
                numBuffer + " available machines desired\n");
        if (numRunning < poolSize){
            // not enough nodes are running... turn some on!
            for (Machine next : offlineMachines) {
                if (!next.isOnline()) {
                    numRunning++;
                    next.setOnline(true);
                    // purposely don't parallelize: google will bark at us if we scale up too fast
                    try {
                        manager.turnOn(next);
                        availableMachines.add(next);
                    } catch (IOException | InterruptedException e) {
                        LOG.error("Error turning on machine " + next, e);
                    }
                    if (numRunning >= poolSize) {
                        break;
                    }
                }
            }
            // if fewer than needed nodes running, turn things on
            // if we don't have enough machines, add however many machines we need to fill pool
            while (numRunning < poolSize) {
                LOG.warn("Only " + numRunning + " machines, but want " + poolSize + "; adding a new machine");
                numRunning++;
                final Machine newMachine = requestMachine(NameGen.newName(), false);
                availableMachines.add(newMachine);
                runningMachines.add(newMachine);
            }
        }
        if (availableMachines.size() < numBuffer) {
            while (availableMachines.size() < getPoolBuffer()) {
                LOG.warn("Only " + availableMachines.size() + " machines are running and unused, but want " + getPoolBuffer() + " available machines; adding a new machine");
                final Machine newMachine = requestMachine(NameGen.newName(), false);
                availableMachines.add(newMachine);
                runningMachines.add(newMachine);
            }
        } else if (numRunning > poolSize) {
            LOG.info(numRunning + " running machines > " + poolSize + " maximum pool size; trying to shut down extra machines");
            // if more than needed nodes are running, turn off any nodes not servicing clients
            for (Machine next : runningMachines) {
                if (!next.isInUse()) {
                    // leave a buffer of unused machines running, even if we've exceeded pool size
                    if (numBuffer--<=0) {
                        LOG.info("Turning off unneeded machine " + next.getHost());
                        numRunning--;
                        turnOff(next);
                        if (numRunning <= poolSize) {
                            break;
                        }
                    }
                }
            }

        }

    }

    private void turnOff(final Machine machine) {
        // get off the fork-join pool to do IO:
        machine.setOnline(false);
        setTimer("Decommission " + machine.getHost(), ()-> {
            try {
                // TODO: ping the machine and have a procedure that warns the user this is going to happen:
                //  also, we should be hooking up a new DNS name to the machine and deleting the one that is in use
                gcloud("instances", "stop", "-q", machine.getHost());
                manager.replaceDNS(machine);
                machine.setInUse(false);
                if (machines.needsMoreMachines(getPoolBuffer(), getPoolSize())) {
                    requestMachine();
                }
            } catch (IOException | InterruptedException e) {
                System.err.println("Error trying to restart " + machine.getHost());
                e.printStackTrace();
            }
        });
    }

    /**
     * The maximum number of machines to allow at one time (prevents creating new machines)
     * @return 150 during business hours, 75 otherwise.
     */
    private int getMaxPoolSize() {
        String poolSize = System.getenv("MAX_POOL_SIZE");
        if (poolSize != null) {
            return Integer.parseInt(poolSize);
        }
        poolSize = System.getProperty("dh-maxPoolSize");
        if (poolSize != null) {
            return Integer.parseInt(poolSize);
        }
        if (isPeakHours()) {
            return 150;
        }
        return 75;
    }

    /**
     * @return The number of warm machines to keep around. Default is 15 during business hours, 5 otherwise.
     */
    private int getPoolBuffer() {
        String poolSize = System.getenv("POOL_BUFFER");
        if (poolSize != null) {
            return Integer.parseInt(poolSize);
        }
        poolSize = System.getProperty("dh-poolBuffer");
        if (poolSize != null) {
            return Integer.parseInt(poolSize);
        }
        if (isPeakHours()) {
            return 15;
        }
        return 5;
    }

    /**
     * @return The desired size of the "always on" pool of machines.
     * During business hours, we keep 20 machines always available, only 5 afterhours.
     * We start adding new machines to keep at least {@link #getPoolBuffer()} (15 biz | 5 afterhours) machines available.
     */
    private int getPoolSize() {
        String poolSize = System.getenv("POOL_SIZE");
        if (poolSize != null) {
            return Integer.parseInt(poolSize);
        }
        poolSize = System.getProperty("dh-poolSize");
        if (poolSize != null) {
            return Integer.parseInt(poolSize);
        }
        if (isPeakHours()) {
            return 20;
        }
        return 5;
    }

    private boolean isPeakHours() {
        LocalTime time = LocalTime.now(TZ_NY);
        return time.isAfter(BIZ_START) && time.isBefore(BIZ_END);
    }

    private void loadMachines() {
        try {
            final Execute.ExecutionResult result = gcloudQuiet(true, false, "instances", "list",
                    "--filter", "labels." + LABEL_PURPOSE + "=" + PURPOSE_WORKER,
                    "--format", "csv[box,no-heading](name,hostname,status,networkInterfaces[0].accessConfigs[0].natIP)",
                    // we'll do paging if we actually see activity this high
                    "--page-size", "500",
                    "-q"
            );
            final String[] lines = result.out.split("\n");
            if (result.out.contains("not present")) {
                // empty, no parsing...
                System.out.println("0 active workers found");
            } else {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Got active workers:");
                    LOG.trace(result.out);
                } else {
                    LOG.info("Got " + lines.length + " total workers (use trace logging to see response)");
                }
                int online = 0;
                for (String line : lines) {
                    if (line.isEmpty()) {
                        continue;
                    }
                    String[] bits = line.split(",");
                    Machine mach = new Machine(bits[0]);
                    // nasty chunk of code I'm uncommenting whenever I want to nuke all workers to use a new worker image... needs a main() somewhere
//                    setTimer("blart", ()->
//                            {
//                                try {
//                                    gcloud("instances", "delete", "-q", mach.getHost());
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                } catch (InterruptedException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                            );
//                    if ("".length() == 0) continue;
                    if (bits[1].length() > 0) {
                        // hmmm.... we really shouldn't be using custom hostname... it's stuck to machine on creation
                        // for now, we'll just prefer a non-null DomainMapping object, if one is assigned to this instance
                        mach.setDomainName(bits[1]);
                    }
                    if (bits[2].length() > 0) {
                        mach.setOnline("RUNNING".equals(bits[2]));
                        if (mach.isOnline()) {
                            online++;
                        }
                    }
                    if (bits.length == 4 && bits[3].length() > 0) {
                        mach.setIp(bits[3]);
                    }
                    machines.addMachine(mach);
                }
                LOG.infof("Found %s running machines", online);
            }

        } catch (IOException | InterruptedException e) {
            LOG.error("Failed to get active workers", e);
        }
        latch.countDown();
    }

    private void loadDomains() {
        latch.countDown();
    }

    private void loadIpsUnused() {
        String[] ipBits = null;
        try {
            Execute.ExecutionResult result = gcloudQuiet(true, false, "addresses", "list",
                    "--filter", "region:" + REGION + " AND status:reserved",
                    "--format", "csv[box,no-heading](name,ADDRESS)",
                    "-q"
            );
            for (String ipRec : result.out.split("\\s+")) {
                if (ipRec.isEmpty()) {
                    continue;
                }
                ipBits = ipRec.split(",");
                String name = ipBits[0];
                String addr = ipBits[1];
                IpMapping ip = new IpMapping(name, addr);
                ips.addIpUnused(ip);
            }
            LOG.info("Found " + ips.getNumUnused() + " unused IP addresses");
        } catch (Exception e) {
            System.err.println("Unable to load IPs! Last seen item: " + (ipBits == null ? null : Arrays.asList(ipBits)));
            e.printStackTrace();
        }
        latch.countDown();
    }

    private void loadIpsUsed() {
        String[] ipBits = null;
        try {
            Execute.ExecutionResult result = gcloudQuiet(true, false, "addresses", "list",
                   // TODO: label our addresses so we can filter more correctly:
                    "--filter", "region:" + REGION + " AND status:in_use",
                    "--format", "csv[box,no-heading](name,ADDRESS)",
                    "-q"
            );
            for (String ipRec : result.out.split("\\s+")) {
                ipBits = ipRec.split(",");
                String name = ipBits[0];
                String addr = ipBits[1];
                if (name.startsWith("dh-") || name.startsWith("perf-")) {
                    // ignore these addresses
                    continue;
                }
                IpMapping ip = new IpMapping(name, addr);
                ips.addIpUsed(ip);
            }
            LOG.info("Found " + ips.getNumUsed() + " used IP addresses");
        } catch (Exception e) {
            System.err.println("Unable to load IPs! Last seen item: " + (ipBits == null ? null : Arrays.asList(ipBits)));
            e.printStackTrace();
        }
        latch.countDown();
    }

    public boolean isReady() {
        return latch.getCount() == 0;
    }

    public void waitUntilReady() {
        try {
            latch.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            String msg = "Interrupted waiting for controller to read metadata; check for error logs!";
            System.err.println(msg);
            Thread.currentThread().interrupt();
            throw new RuntimeException(msg, e);
        }
    }

    public Machine requestMachine() {
        waitUntilReady();
        Optional<Machine> machine = machines.maybeGetMachine(manager);
        if (machine.isPresent()) {
            final Machine mach = machine.get();
            final IpMapping ip = ips.reserveIp(this, mach, null);
            moveToRunningState(mach, ip, true);
            LOG.info("Sending user to pre-existing machine " + mach);
            return mach;
        }
        // hm... we should probably send user to interstitial page immediately...
        // no need to have them wait until machine spins up to see "you gonna have to wait" screen.
        String newName = NameGen.newName();
        LOG.info("Sending user to new machine " + newName);
        return requestMachine(NameGen.newName(), true);
    }

    public Machine requestMachine(String name, boolean reserve) {
        // reserve an IP while getting a machine going.
        final IpMapping ip = ips.getUnusedIp(this);
        final Machine machine = machines.createMachine(manager, name, ip);
        moveToRunningState(machine, ip, reserve);
        return machine;
    }

    void reloadMetadata(Machine machine) {
        // unless this machine is currently running a metadata reload, start one.

    }

    /**
     * Handles all the work to make sure a worker we believe to be runnable is actually online and correctly routed to DNS.
     *
     * @param machine The machine to turn on
     * @param ip An ip address to use for this machine
     * @param reserve Whether or not to actually claim the machine, or just turn it on and let it idle.
     */
    void moveToRunningState(final Machine machine, final IpMapping ip, final boolean reserve) {
        LOG.info("Moving machine " + machine.getDomainName() + " to a running state");
        if (reserve) {
            machine.setExpiry(System.currentTimeMillis() + getSessionTtl());
            machine.setInUse(true);
        }
        setTimer("Move to running state", ()->{
             Execute.ExecutionResult result;
            String myIp = machine.getIp();
            try {
                String purpose = machine.isController() ? PURPOSE_CONTROLLER :
                                 machine.isSnapshotCreate() ? PURPOSE_CREATOR :
                                PURPOSE_WORKER;
                result = gcloud(true,"instances",
                        "describe", machine.getHost(),
                        "-q",
                        "--format=csv[box,no-heading](labels." + LABEL_PURPOSE + ",networkInterfaces[0].accessConfigs[0].natIP)");// add ,status, and turn offline machines on
                String data = result.out.trim();
                if (!data.startsWith(purpose + ",")) {
                    LOG.warn("Instance " + machine.getHost() + " had label '" + data.split(",")[0] + "' instead of " + purpose + "; fixing....");
                    result = gcloud(true,"instances", "add-labels", machine.getHost(), "--labels=" + LABEL_PURPOSE + "=" + purpose);
                    if (result.code != 0) {
                        // check if this was a "not found" message
                        if (result.out.contains("not found")) {
                            // yikes! the machine doesn't exist... create one w/ the specified ip address (by name, so it is stable!)
                            if (machines.getNumberMachines() > getMaxPoolSize()) {
                                LOG.error("Refusing to create more than " + getMaxPoolSize() + " machines (currently " + machines.getNumberMachines() + ")");
                            }
                            machine.setIp(ip.getName());
                            manager.createMachine(machine);
                            ips.addIpUsed(ip);
                            result.code = 0;
                        }
                    }
                    if (result.code != 0) {
                        final String msg = "Unable to add label to machine " + machine.getHost();
                        LOG.error(msg);
                        GoogleDeploymentManager.warnResult(result);
                        return;
                    }
                }
                if (!data.endsWith("," + myIp)) {
                    LOG.warn("Replacing " + myIp + " with " + data.split(",")[1]);
                    if (data.contains(",")) {
                        machine.setIp(data.split(",")[1]);
                    } else {
                        LOG.error("Unexpected result: " + result.out);
                    }
                }
            } catch (Exception e) {
                LOG.error("Unable to move machine to running state: " + machine + " ip: " + ip, e);
            }
            // machine is alive, make sure it has DNS!
            final Execute.ExecutionResult dnsCheck = manager.checkDns(machine.getDomainName(), DNS_QUAD9);
            if (dnsCheck.code == 0 && dnsCheck.out.trim().equals(myIp)) {
                // machine is alive, DNS is all good.
                LOG.trace("DNS resolved for " + machine.getDomainName());
            } else {
                LOG.warn("DNS does not resolve correctly for " + machine.getDomainName() + " @ " + machine.getIp() + "; setting up DNS records");
                // machine is NOT alive, glue on some ad-hoc DNS
                try {
                    setupDns(machine, ip);
                } catch (Exception e) {
                    LOG.error("Unable to setup DNS for machine " + machine, e);
                }
            }

        });
    }

    private long getSessionTtl() {
        return TimeUnit.MINUTES.toMillis(45);
    }

    private void setupDns(final Machine machine, final IpMapping ip) {
        final String machineIp = machine.getIp();
        if (machineIp == null || machineIp.isEmpty()) {
            final IpMapping mapping = ips.reserveIp(this, machine, ip);
            ensureAccessConfig(machine, mapping);
            machine.setIp(mapping.getIp());
            ips.addIpUsed(mapping);
        } else {
            if (machineIp.indexOf('.') == -1 && machineIp.indexOf(':') == -1) {
                final IpMapping mapping = ips.reserveIp(this, machine, ip);
                // TODO: make sure this ip mapping is the correct access-config:
                //    https://cloud.google.com/compute/docs/ip-addresses/reserve-static-external-ip-address#IP_assign
                ensureAccessConfig(machine, mapping);
                machine.setIp(mapping.getIp());
                ips.addIpUsed(mapping);
            }
        }

        manager.assignDns(Stream.of(machine));
    }

    private void ensureAccessConfig(final Machine machine, final IpMapping mapping) {
    }

    public IpMapping requestIp() {
        waitUntilReady();
        return ips.getUnusedIp(this);
    }
    public Collection<IpMapping> requestNewIps(int numIps) {
        final List<IpMapping> list = new ArrayList<>();

        while (numIps --> 0) {
            final IpMapping mapping = new IpMapping(NameGen.newName(), null);
            list.add(mapping);
        }
        // Filling in the IP address requires IO, so let's do that offthread...
        // IMPORTANT: code calling us may be holding a lock, so don't de-off-thread this code without checking callers of this method for locks!
        setTimer("Get or create " + list.size() + "IPs", ()-> list.parallelStream().forEach(this::getOrCreateIp));

        return Collections.unmodifiableList(list);
    }

    protected void getOrCreateIp(final IpMapping ip) {
        if (ip.getIp() == null) {
            // create a new IP w/ google.
            String name = ip.getName();
            try {
                final Execute.ExecutionResult result = gcloud(true, false, "addresses", "create",
                        name, "--region", NameConstants.REGION);
                if (result.code != 0) {
                    String msg = "Unable to create an ip address for " + ip;
                    System.err.println(msg);
                    System.err.println("stdout:");
                    System.err.println(result.out);
                    System.err.println("stderr:");
                    System.err.println(result.err);
                    throw new IllegalStateException(msg);
                }
                // address exists... parse out the value!
                System.out.println("Created address for " + ip + " :\n" + result.out);
                ips.addIpUnused(ip);
            } catch (IOException | InterruptedException e) {
                System.err.println("Unable to create an IP address for " + ip + "; check for resource quotas / service outage?");
                e.printStackTrace();
            }
        }
    }

    public boolean isMachineReady(final String domainName) {
        String uri = "https://" + domainName + "/health";
        final Request req = new Request.Builder().get()
                .url(uri)
                .build();
        final Response response;
        try {
            response = client.newCall(req).execute();
        } catch (IOException e) {
            return false;
        }
        boolean success = response.isSuccessful();
        response.close();
        return success;
    }

    public GoogleDeploymentManager getDeploymentManager() {
        return manager;
    }

    public void waitUntilHealthy(final Machine machine) throws IOException, InterruptedException {
        final String key = "finished code: ";
        final String failed = "ran out of tries";
        final Execute.ExecutionResult result = Execute.ssh(true, machine.getDomainName(), //"bash", "-c",
//        GoogleDeploymentManager.gcloud(false, true, "ssh", machine.getHost(),
//                "--command",
                        "function watch_logs() {\n" +
                        "  echo Watching log file /var/log/vm-startup.log\n" +
                        "  while ! test -f /var/log/vm-startup.log ; do sleep 1 ; done\n" +
                        "  tail -f /var/log/vm-startup.log\n" +
                        "}\n" +
                        "function wait_til_ready() {\n" +
                        "  # we sleep 1 per try, so 720 tries > 12 minutes after ssh is online\n" +
                        "  local tries=720\n" +
                        "  echo waiting for localhost:10000 to be responsive\n" +
                        "  watch_logs &\n" +
                        "  pid=$!\n" +
                        "  while (( tries > 0 )) && ! curl -k https://localhost:10000/health &> /dev/null; do\n" +
                        "    tries=$((tries-1))\n" +
                        "    (( tries%10 )) || echo \"start-monitor tries remaining: $tries\"\n" +
                        "    sleep 1\n" +
                        "  done\n" +
                        "  kill $pid\n" +
                        "  if (( tries > 0 )); then\n" +
                        "    echo \"localhost:10000 is responsive; $(hostname) is alive!\"\n" +
                        "  else\n" +
                        "    echo Tried 720 times to reach https://localhost:10000/health but " + failed + "\n" +
                        "  fi\n" +
                        "} ; TIMEFORMAT='\n" +
                        "wait_til_ready exited after: %3Rs' ; time wait_til_ready ; code=$? ; " +
                        "echo " + key + "${code} ; echo ; sleep 1 ; kill $PPID "
        );
        int realCodeLoc = result.out.lastIndexOf(key);
        if (result.out.contains(failed)) {
            throw new IllegalStateException("wait_til_ready failed:\n\n" + result.out);
        }
        String code = result.out.substring(realCodeLoc + key.length(), result.out.indexOf('\n', realCodeLoc + key.length()));
        if (!"0".equals(code)) {
            throw new IllegalStateException("wait_til_ready returned code " + code);
        }
    }
}
