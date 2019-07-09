package com.grayraccoon.webscrapeitems.processing;

public class TrimPostProcess implements PostProcess {

	public TrimPostProcess() {}

	@Override
	public String postProcess(String target) {
		return target.trim();
	}

}
