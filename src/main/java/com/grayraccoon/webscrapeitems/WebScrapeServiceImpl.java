package com.grayraccoon.webscrapeitems;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.grayraccoon.webscrapeitems.models.*;
import com.grayraccoon.webscrapeitems.processing.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class WebScrapeServiceImpl implements WebScrapeService {

	private static final Logger LOGGER = LoggerFactory.getLogger(WebScrapeServiceImpl.class);

	private ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Set<String>> fetchAllSubPlainItemsFrom(FetchModel fetchModel) {
		Objects.requireNonNull(fetchModel.getPlainFetchModels());
		Objects.requireNonNull(fetchModel.getSources());

		Map<String, Set<String>> allPlainObjects = new HashMap<>();

		for (String source: fetchModel.getSources()) {

			Map<String, Set<String>> sourcePlainObjects = fetchAllSubPlainItemsFrom(source, fetchModel);

			for (BasicFetchModel basicFetchModel: fetchModel.getPlainFetchModels()) {
				final String currentKey = basicFetchModel.getPlainObjectName();
				if (!allPlainObjects.containsKey(currentKey)) {
					allPlainObjects.put(currentKey, new HashSet<>());
				}
				allPlainObjects.get(currentKey).addAll(
						sourcePlainObjects.get(currentKey)
				);
			}
		}

		return allPlainObjects;
	}

	private Map<String, Set<String>> fetchAllSubPlainItemsFrom(String source, FetchModel fetchModel) {
		Map<String, Set<String>> sourcePlainObjects = new HashMap<>();

		try (final WebClient webClient = new WebClient()) {
			this.configureWebClient(webClient, fetchModel);

			HtmlPage page = webClient.getPage(source);
			boolean newPageLoaded = true;

			while(newPageLoaded && page != null) {

				for (final BasicFetchModel basicFetchModel : fetchModel.getPlainFetchModels()) {
					final String currentKey = basicFetchModel.getPlainObjectName();

					Set<String> foundItems = new HashSet<>();

					if (this.isInvalidSelector(basicFetchModel.getItemSelector())) {
						break;
					}
					List<DomNode> nodes = this.applySelectorAll(basicFetchModel.getItemSelector(), page);

					if (nodes != null) {
						nodes.forEach(domNode -> {
							String plainValueStr = this.applyFieldGetter(domNode, basicFetchModel.getSinglePlainObject());
							foundItems.add(plainValueStr);
						});
					}

					if (!sourcePlainObjects.containsKey(currentKey)) {
						sourcePlainObjects.put(currentKey, new HashSet<>());
					}
					sourcePlainObjects.get(currentKey).addAll(foundItems);
				}

				page = tryToLoadNextPage(fetchModel, page);
				newPageLoaded = page != null;
			}

		} catch (IOException e) {
			LOGGER.error("There was an error with the WebClient: ", e);
		}

		return sourcePlainObjects;
	}

	private HtmlPage tryToLoadNextPage(FetchModel fetchModel, HtmlPage page) throws IOException {
		if (this.isInvalidSelector(fetchModel.getNextButtonSelector())) {
			return null;
		}
		List<DomNode> nextButtons = this.applySelectorAll(fetchModel.getNextButtonSelector(), page);

		if (nextButtons != null && nextButtons.size() > 0) {
			HtmlElement nextLink = (HtmlElement) nextButtons.get(0);
			return nextLink.click();
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> Set<T> fetchAllItemsFrom(FetchModel fetchModel, Class<T> typeKey) {
		Objects.requireNonNull(fetchModel.getSources());
		Objects.requireNonNull(typeKey);

		Set<T> allItems = new HashSet<>();
		for (String source: fetchModel.getSources()) {
			allItems.addAll( fetchAllItemsFrom(source, fetchModel, typeKey) );
		}
		return allItems;
	}

	private  <T> Set<T> fetchAllItemsFrom(String source, FetchModel fetchModel, Class<T> typeKey) {
		Set<T> foundItems = new HashSet<>();
		try (final WebClient webClient = new WebClient()) {
			this.configureWebClient(webClient, fetchModel);

			HtmlPage page = webClient.getPage(source);
			boolean newPageLoaded = true;

			while(newPageLoaded && page != null) {

				if (this.isInvalidSelector(fetchModel.getItemSelector())) {
					break;
				}
				List<DomNode> nodes = this.applySelectorAll(fetchModel.getItemSelector(), page);

				if (nodes != null) {
					nodes.forEach(domNode -> {
						if (fetchModel.getSinglePlainObject() != null) {
							String plainValueStr = this.applyFieldGetter(domNode, fetchModel.getSinglePlainObject());
							final T plainValue = objectMapper.convertValue(plainValueStr, typeKey);
							foundItems.add(plainValue);
						} else {
							Objects.requireNonNull(fetchModel.getFields());
							Map<String, String> itemMap = new HashMap<>();

							for (FieldGetter getter: fetchModel.getFields()) {
								itemMap.put(getter.getFieldName(), this.applyFieldGetter(domNode, getter));
							}

							T item = objectMapper.convertValue(itemMap, typeKey);
							if  (item != null) {
								foundItems.add(item);
							}
						}
					});
				}

				page = tryToLoadNextPage(fetchModel, page);
				newPageLoaded = page != null;
			}

		} catch (IOException e) {
			LOGGER.error("There was an error with the WebClient: ", e);
		}
		return foundItems;
	}

	private String applyFieldGetter(DomNode domNode, FieldGetter getter) {
		HtmlElement targetNode;

		if (getter == null || getter.getGetterMethod() == FieldGetter.GETTER_METHOD_OFF) {
			return "";
		}

		if (this.isInvalidSelector(getter.getSelector())) {
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
				case PostProcessModel.TYPE_REMOVE:
					postProcess = new RemovePostProcess(postProcessModel.getArguments());
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


	private boolean isInvalidSelector(Selector selector) {
		return selector == null
				|| selector.getSelector() == null
				|| selector.getSelector().isEmpty()
				|| (selector.getType() != Selector.TYPE_CSS
				&& selector.getType() != Selector.TYPE_XPATH)
				;
	}

	private HtmlElement applySelector(Selector selector, DomNode node) {
		if (this.isInvalidSelector(selector)) {
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
		if (this.isInvalidSelector(selector)) {
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

		webClient.getOptions().setTimeout(15000);
		webClient.getOptions().setPrintContentOnFailingStatusCode(false);

		webClient.setCssErrorHandler(new SilentCssErrorHandler());
	}

}
