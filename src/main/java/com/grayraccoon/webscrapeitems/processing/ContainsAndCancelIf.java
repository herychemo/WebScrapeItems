package com.grayraccoon.webscrapeitems.processing;

import java.util.List;

public class ContainsAndCancelIf extends CancelIf {

	public ContainsAndCancelIf() {}

	public ContainsAndCancelIf(List<String> args) {
		super(args);
	}

	@Override
	public boolean testCondition(String result) {
		if (getArgs() == null) {
			return false;
		}

		boolean containsAll = true;

		for (String arg: getArgs()) {
			if (!result.contains(arg)) {
				containsAll = false;
			}
		}

		return containsAll;

	}

}
