package io.deephaven.web.shared.ide.lsp;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsType;
import jsinterop.base.JsPropertyMap;

import java.io.Serializable;

@JsType(namespace = "dh.lsp")
public class CompletionContext implements Serializable {
    public int triggerKind;
    public String triggerCharacter;

    public CompletionContext() {}

    @JsIgnore
    public CompletionContext(JsPropertyMap<Object> source) {
        this();

        if (source.has("triggerKind")) {
            triggerKind = source.getAny("triggerKind").asInt();
        }

        if (source.has("triggerCharacter")) {
            triggerCharacter = source.getAny("triggerCharacter").asString();
        }
    }
}
