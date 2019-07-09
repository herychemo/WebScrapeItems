package com.grayraccoon.webscrapeitems;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.grayraccoon.webscrapeitems.models.*;
import com.grayraccoon.webscrapeitems.processing.*;

import java.io.IOException;
import java.util.*;

public class WebScrapeService {

	private ObjectMapper objectMapper = new ObjectMapper();

	public <T> Set<T> fetchAllItemsFrom(FetchModel fetchModel, Class<T> typeKey) {
		Set<T> allItems = new HashSet<>();
		for (String source: fetchModel.getSources()) {
			allItems.addAll( fetchAllItemsFrom(source, fetchModel, typeKey) );
		}
		return allItems;
	}

	public <T> Set<T> fetchAllItemsFrom(String source, FetchModel fetchModel, Class<T> typeKey) {
		Set<T> sourcesList = new HashSet<>();
		try (final WebClient webClient = new WebClient()) {
			this.configureWebClient(webClient, fetchModel);

			HtmlPage page = webClient.getPage(source);
			boolean newPageLoaded = true;

			while(newPageLoaded && page != null) {

				if (!this.isValidSelector(fetchModel.getItemSelector())) {
					break;
				}
				List<DomNode> nodes = this.applySelectorAll(fetchModel.getItemSelector(), page);

				if (nodes != null) {
					nodes.forEach(domNode -> {
						if (fetchModel.getPlainObject() != null) {
							String plainValueStr = this.applyFieldGetter(domNode, fetchModel.getPlainObject());
							final T plainValue = objectMapper.convertValue(plainValueStr, typeKey);
							sourcesList.add(plainValue);
						} else {
							Objects.requireNonNull(fetchModel.getFields());
							Map<String, String> itemMap = new HashMap<>();

							for (FieldGetter getter: fetchModel.getFields()) {
								itemMap.put(getter.getFieldName(), this.applyFieldGetter(domNode, getter));
							}

							T item = objectMapper.convertValue(itemMap, typeKey);
							if  (item != null) {
								sourcesList.add(item);
							}
						}
					});
				}

				newPageLoaded = false;

				if (!this.isValidSelector(fetchModel.getNextButtonSelector())) {
					break;
				}
				List<DomNode> nextButtons = this.applySelectorAll(fetchModel.getNextButtonSelector(), page);

				if (nextButtons != null && nextButtons.size() > 0) {
					HtmlElement nextLink = (HtmlElement) nextButtons.get(0);
					page = nextLink.click();
					newPageLoaded = true;
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return sourcesList;
	}

	private String applyFieldGetter(DomNode domNode, FieldGetter getter) {
		HtmlElement targetNode;

		if (getter == null || getter.getGetterMethod() == FieldGetter.GETTER_METHOD_OFF) {
			return "";
		}

		if ( !this.isValidSelector(getter.getSelector()) ) {
			targetNode = (HtmlElement) domNode;
		}else {
			targetNode = this.applySelector(getter.getSelector(), domNode);
		}

		if (targetNode == null) {
			return "";
		}

		String field;

		switch (getter.getGetterMethod()) {
			case FieldGetter.GETTER_METHOD_TEXT:
			default:
				field = targetNode.getTextContent();
				break;
			case FieldGetter.GETTER_METHOD_ATTR:
				field = targetNode.getAttribute(getter.getGetterAttr());
				break;
		}

		if (field == null) {
			return "";
		}

		for (PostProcessModel postProcessModel: getter.getPostProcessModels()) {
			PostProcess postProcess;
			switch (postProcessModel.getType()) {
				case PostProcessModel.TYPE_TRIM:
					postProcess = new TrimPostProcess();
					break;
				case PostProcessModel.TYPE_SPLIT:
					postProcess = new SplitPostProcess(postProcessModel.getArguments());
					break;
					default:
						continue;
			}
			field = postProcess.postProcess(field);
		}

		for (CancelIfModel cancelIfModel: getter.getCancelIfModels()) {
			CancelIf cancelIf;
			switch (cancelIfModel.getType()) {
				case CancelIfModel.TYPE_CONTAINS_OR:
					cancelIf = new ContainsOrCancelIf(cancelIfModel.getArguments());
					break;
				case CancelIfModel.TYPE_CONTAINS_AND:
					cancelIf = new ContainsAndCancelIf(cancelIfModel.getArguments());
					break;
				default:
					continue;
			}
			if (cancelIf.testCondition(field)) {
				return "";
			}
		}

		return field;
	}


	private boolean isValidSelector(Selector selector) {
		return selector != null
				&& selector.getSelector() != null
				&& !selector.getSelector().isEmpty()
				&& (selector.getType() == Selector.TYPE_CSS
				|| selector.getType() == Selector.TYPE_XPATH)
				;
	}

	private HtmlElement applySelector(Selector selector, DomNode node) {
		if (!this.isValidSelector(selector)) {
			return null;
		}
		switch (selector.getType()) {
			case Selector.TYPE_CSS:
			default:
				return node.querySelector(selector.getSelector());
			case Selector.TYPE_XPATH:
				return node.getFirstByXPath(selector.getSelector());
		}
	}

	private List<DomNode> applySelectorAll(Selector selector, DomNode node) {
		if (!this.isValidSelector(selector)) {
			return null;
		}
		switch (selector.getType()) {
			case Selector.TYPE_CSS:
			default:
				return node.querySelectorAll(selector.getSelector());
			case Selector.TYPE_XPATH:
				return node.getByXPath(selector.getSelector());
		}
	}

	private void configureWebClient(WebClient webClient, FetchModel fetchModel) {
		webClient.getCookieManager().setCookiesEnabled(true);
		webClient.getOptions().setUseInsecureSSL(false);
		webClient.getOptions().setPopupBlockerEnabled(false);

		webClient.getOptions().setScreenWidth(420);
		webClient.getOptions().setScreenHeight(600);

		webClient.getOptions().setJavaScriptEnabled(fetchModel.isUsingJavascript());
		webClient.getOptions().setThrowExceptionOnScriptError(false);
		webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
		webClient.getOptions().setCssEnabled(true);

		webClient.getOptions().setTimeout(60000);
		webClient.getOptions().setPrintContentOnFailingStatusCode(false);

		webClient.setCssErrorHandler(new SilentCssErrorHandler());
	}

}
