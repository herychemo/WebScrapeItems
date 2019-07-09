package com.grayraccoon.webscrapeitems.processing;

import java.util.ArrayList;
import java.util.List;

public class RemovePostProcess implements PostProcess {

    private List<String> removeStrings;

    public RemovePostProcess() {
        this.removeStrings = new ArrayList<>();
    }

    public RemovePostProcess(List<String> removeStrings) {
        super();
        if (removeStrings != null) {
            this.removeStrings = removeStrings;
        }
    }

    public List<String> getRemoveStrings() {
        return removeStrings;
    }

    @Override
    public String postProcess(String target) {
        for (String string : this.removeStrings) {
            target = target.replace(string, "");
        }
        return target;
    }
}
