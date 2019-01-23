package com.optimizedproductions.webscrapeitems;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.optimizedproductions.webscrapeitems.models.CancelIfModel;
import com.optimizedproductions.webscrapeitems.models.FetchModel;
import com.optimizedproductions.webscrapeitems.models.FieldGetter;
import com.optimizedproductions.webscrapeitems.models.PostProcessModel;
import com.optimizedproductions.webscrapeitems.processing.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
				DomNodeList<DomNode> nodes = page.querySelectorAll(fetchModel.getItemSelector());
				if (nodes != null) {
					nodes.forEach(domNode -> {

						Map<String, String> itemMap = new HashMap<>();

						for (FieldGetter getter: fetchModel.getFields()) {
							itemMap.put(getter.getFieldName(), this.applyFieldGetter(domNode, getter));
						}

						T item = objectMapper.convertValue(itemMap, typeKey);
						if  (item != null) {
							sourcesList.add(item);
						}
					});
				}

				newPageLoaded = false;
				DomNodeList<DomNode> nextButtons = page.querySelectorAll(fetchModel.getNextButtonSelector());

				if (nextButtons != null && nextButtons.getLength() > 0) {
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

		if (getter.getSelector() == null || getter.getSelector().isEmpty()) {
			targetNode = (HtmlElement) domNode;
		}else {
			targetNode = domNode.querySelector( getter.getSelector() );
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
