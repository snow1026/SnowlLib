package io.github.snow1026.snowlib.api.attribute;

import io.github.snow1026.snowlib.SnowKey;
import io.github.snow1026.snowlib.api.component.attribute.AttributeComponent;

public class AttributeBuilder {
    private final SnowKey key;
    private double def;
    private double min;
    private double max;
    private boolean sync;

    public AttributeBuilder(SnowKey key) {
        this.key = key;
    }

    public AttributeBuilder def(double def) {
        this.def = def;
        return this;
    }

    public AttributeBuilder min(double min) {
        this.min = min;
        return this;
    }

    public AttributeBuilder max(double max) {
        this.max = max;
        return this;
    }

    public AttributeBuilder sync(boolean sync) {
        this.sync = sync;
        return this;
    }

    public SnowAttribute build() {
        AttributeComponent component = new AttributeComponent(this.def, this.min, this.max, this.sync);
        return new SnowAttribute(this.key, component) {};
    }
}
