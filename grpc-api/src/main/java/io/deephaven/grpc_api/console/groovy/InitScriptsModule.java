package io.deephaven.grpc_api.console.groovy;

import io.deephaven.db.util.GroovyDeephavenSession.CountMetrics;
import io.deephaven.db.util.GroovyDeephavenSession.Db;
import io.deephaven.db.util.GroovyDeephavenSession.InitScript;
import io.deephaven.db.util.GroovyDeephavenSession.PerformanceQueries;
import io.deephaven.db.util.GroovyDeephavenSession.RunScripts;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import java.util.Set;

public class InitScriptsModule {

    @Module
    public interface Explicit {
        @Binds
        @IntoSet
        InitScript bindsDbScripts(Db impl);

        @Binds
        @IntoSet
        InitScript bindsCountMetricsScripts(CountMetrics impl);

        @Binds
        @IntoSet
        InitScript bindsPerformanceQueriesScripts(PerformanceQueries impl);

        @Provides
        static RunScripts providesRunScriptLogic(Set<InitScript> scripts) {
            return RunScripts.of(scripts);
        }
    }

    @Module
    public interface ServiceLoader {

        @Provides
        static RunScripts providesRunScriptLogic() {
            return RunScripts.serviceLoader();
        }
    }

    @Module
    public interface OldConfig {

        @Provides
        static RunScripts providesRunScriptLogic() {
            return RunScripts.oldConfiguration();
        }
    }
}
