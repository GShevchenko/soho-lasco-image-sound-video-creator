package org.example.domain;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MetaTotal implements Serializable {

    private Integer total;
    private List<ImageMetadata> data;

    public MetaTotal() {
        data = new ArrayList<>();
    }
    public Integer addMetadata(MetaTotal additionalMeta) {
        this.total += additionalMeta.getTotal();
        data.addAll(additionalMeta.getData());
        return total;
    }

    public Collection<? extends ImageMetadata> getData() {
        return data;
    }

    public Integer getTotal() {
        return total;
    }

}
