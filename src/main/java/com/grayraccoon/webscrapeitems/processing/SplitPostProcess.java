package com.grayraccoon.webscrapeitems.processing;

import java.util.ArrayList;
import java.util.List;

public class SplitPostProcess implements PostProcess {

	private List<String> splitRegex;

	public SplitPostProcess() {
		this.splitRegex = new ArrayList<>();
	}

	public SplitPostProcess(List<String> splitRegex) {
		this();
		if (splitRegex != null) {
			this.splitRegex = splitRegex;
		}
	}

	public List<String> getSplitRegex() {
		return splitRegex;
	}

	@Override
	public String postProcess(String target) {
		for (String regex : this.splitRegex) {
			target = target.split(regex)[0];
		}
		return target;
	}

}
