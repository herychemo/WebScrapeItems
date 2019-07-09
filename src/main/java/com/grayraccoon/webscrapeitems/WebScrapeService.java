package com.grayraccoon.webscrapeitems;

import com.grayraccoon.webscrapeitems.models.FetchModel;

import java.util.Map;
import java.util.Set;

public interface WebScrapeService {

    /**
     * Fetch multiple plain string lists from a page.
     *  All the fetch specification must be defined on the fetchModel (sources, plainFetchModels).
     * Useful when you want to extract multiple plain string lists from same page.
     *
     * @param fetchModel
     * @return
     */
    Map<String, Set<String>> fetchAllSubPlainItemsFrom(FetchModel fetchModel);

    /**
     * Fetch all a group of selectors as a POJO defined by T.
     * All the fetch specification must be defined on the fetchModel (sources, fields).
     * Useful when you want to fetch a list of items, every item with its own properties.
     *
     * @param fetchModel Definition of what you require to fetch.
     * @param typeKey Type of object to map the resulted items.
     * @param <T> Type of object to map the resulted items.
     * @return A set of all found items on every source.
     */
    <T> Set<T> fetchAllItemsFrom(FetchModel fetchModel, Class<T> typeKey);

}
