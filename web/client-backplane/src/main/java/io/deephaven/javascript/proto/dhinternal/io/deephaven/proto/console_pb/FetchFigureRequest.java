package io.deephaven.javascript.proto.dhinternal.io.deephaven.proto.console_pb;

import elemental2.core.Uint8Array;
import io.deephaven.javascript.proto.dhinternal.io.deephaven.proto.ticket_pb.Ticket;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;

@JsType(
        isNative = true,
        name = "dhinternal.io.deephaven.proto.console_pb.FetchFigureRequest",
        namespace = JsPackage.GLOBAL)
public class FetchFigureRequest {
    @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
    public interface ToObjectReturnType {
        @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
        public interface SourceIdFieldType {
            @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
            public interface GetTicketUnionType {
                @JsOverlay
                static FetchFigureRequest.ToObjectReturnType.SourceIdFieldType.GetTicketUnionType of(
                        Object o) {
                    return Js.cast(o);
                }

                @JsOverlay
                default String asString() {
                    return Js.asString(this);
                }

                @JsOverlay
                default Uint8Array asUint8Array() {
                    return Js.cast(this);
                }

                @JsOverlay
                default boolean isString() {
                    return (Object) this instanceof String;
                }

                @JsOverlay
                default boolean isUint8Array() {
                    return (Object) this instanceof Uint8Array;
                }
            }

            @JsOverlay
            static FetchFigureRequest.ToObjectReturnType.SourceIdFieldType create() {
                return Js.uncheckedCast(JsPropertyMap.of());
            }

            @JsProperty
            FetchFigureRequest.ToObjectReturnType.SourceIdFieldType.GetTicketUnionType getTicket();

            @JsProperty
            void setTicket(
                    FetchFigureRequest.ToObjectReturnType.SourceIdFieldType.GetTicketUnionType ticket);

            @JsOverlay
            default void setTicket(String ticket) {
                setTicket(
                        Js.<FetchFigureRequest.ToObjectReturnType.SourceIdFieldType.GetTicketUnionType>uncheckedCast(
                                ticket));
            }

            @JsOverlay
            default void setTicket(Uint8Array ticket) {
                setTicket(
                        Js.<FetchFigureRequest.ToObjectReturnType.SourceIdFieldType.GetTicketUnionType>uncheckedCast(
                                ticket));
            }
        }

        @JsOverlay
        static FetchFigureRequest.ToObjectReturnType create() {
            return Js.uncheckedCast(JsPropertyMap.of());
        }

        @JsProperty
        FetchFigureRequest.ToObjectReturnType.SourceIdFieldType getSourceId();

        @JsProperty
        void setSourceId(FetchFigureRequest.ToObjectReturnType.SourceIdFieldType sourceId);
    }

    @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
    public interface ToObjectReturnType0 {
        @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
        public interface SourceIdFieldType {
            @JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
            public interface GetTicketUnionType {
                @JsOverlay
                static FetchFigureRequest.ToObjectReturnType0.SourceIdFieldType.GetTicketUnionType of(
                        Object o) {
                    return Js.cast(o);
                }

                @JsOverlay
                default String asString() {
                    return Js.asString(this);
                }

                @JsOverlay
                default Uint8Array asUint8Array() {
                    return Js.cast(this);
                }

                @JsOverlay
                default boolean isString() {
                    return (Object) this instanceof String;
                }

                @JsOverlay
                default boolean isUint8Array() {
                    return (Object) this instanceof Uint8Array;
                }
            }

            @JsOverlay
            static FetchFigureRequest.ToObjectReturnType0.SourceIdFieldType create() {
                return Js.uncheckedCast(JsPropertyMap.of());
            }

            @JsProperty
            FetchFigureRequest.ToObjectReturnType0.SourceIdFieldType.GetTicketUnionType getTicket();

            @JsProperty
            void setTicket(
                    FetchFigureRequest.ToObjectReturnType0.SourceIdFieldType.GetTicketUnionType ticket);

            @JsOverlay
            default void setTicket(String ticket) {
                setTicket(
                        Js.<FetchFigureRequest.ToObjectReturnType0.SourceIdFieldType.GetTicketUnionType>uncheckedCast(
                                ticket));
            }

            @JsOverlay
            default void setTicket(Uint8Array ticket) {
                setTicket(
                        Js.<FetchFigureRequest.ToObjectReturnType0.SourceIdFieldType.GetTicketUnionType>uncheckedCast(
                                ticket));
            }
        }

        @JsOverlay
        static FetchFigureRequest.ToObjectReturnType0 create() {
            return Js.uncheckedCast(JsPropertyMap.of());
        }

        @JsProperty
        FetchFigureRequest.ToObjectReturnType0.SourceIdFieldType getSourceId();

        @JsProperty
        void setSourceId(FetchFigureRequest.ToObjectReturnType0.SourceIdFieldType sourceId);
    }

    public static native FetchFigureRequest deserializeBinary(Uint8Array bytes);

    public static native FetchFigureRequest deserializeBinaryFromReader(
            FetchFigureRequest message, Object reader);

    public static native void serializeBinaryToWriter(FetchFigureRequest message, Object writer);

    public static native FetchFigureRequest.ToObjectReturnType toObject(
            boolean includeInstance, FetchFigureRequest msg);

    public native void clearSourceId();

    public native Ticket getSourceId();

    public native boolean hasSourceId();

    public native Uint8Array serializeBinary();

    public native void setSourceId();

    public native void setSourceId(Ticket value);

    public native FetchFigureRequest.ToObjectReturnType0 toObject();

    public native FetchFigureRequest.ToObjectReturnType0 toObject(boolean includeInstance);
}
