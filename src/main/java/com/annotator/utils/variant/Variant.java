package com.annotator.utils.variant;

import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class Variant {
    private final String chr;
    private final String ref;
    private final String alt;
    private final JsonObject variantJson;

    private final int startPos;
    private final int endPos;
    private final String color;
    private final List<String> genes;
    private final String label;

    public Variant(String chr, String pos, String ref, String alt, JsonObject variantJson) {
        this.chr = chr;
        this.ref = ref;
        this.alt = alt;
        this.variantJson = variantJson;

        this.startPos = Integer.parseInt(pos);
        this.endPos = startPos + ref.length() - 1;
        this.color = "grey"; // TODO: Implement...
        this.genes = new ArrayList<>(); // TODO: Implement...
        this.label = constructLabel();
    }

    public String getChr() {
        return this.chr;
    }

    public String getRef() {
        return this.ref;
    }

    public String getAlt() {
        return this.alt;
    }

    public JsonObject getVariantJson() {
        return this.variantJson;
    }

    public int getStartPos() {
        return this.startPos;
    }

    public int getEndPos() {
        return this.endPos;
    }

    public String getColor() {
        return this.color;
    }

    public String getGenes() {
        return this.genes.toString();
    }

    public String getLabel() {
        return this.label;
    }

    private String constructLabel() {
        StringBuilder genesString = new StringBuilder("[");
        for (int i = 0; i < genes.size(); i++) {
            genesString.append(genes.get(i));
            if (i < genes.size() - 1) {
                genesString.append(",");
            }
        }
        genesString.append("]");

        String chrString = "chr" + chr;

        String posString;
        if (startPos != endPos) {
            posString = startPos + "-" + endPos;
        } else {
            posString = String.valueOf(startPos);
        }

        String variantString = ref + ">" + alt;

        return genesString + " " + chrString + ":" + posString + " " + variantString;
    }
}
