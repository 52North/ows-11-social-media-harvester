/**
 * ﻿Copyright (C) 2015 - 2015 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * icense version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.socialmedia.harvester;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.joda.time.DateTime;
import org.n52.instagram.model.InstagramLocation;
import org.n52.socialmedia.DecodingException;
import org.n52.socialmedia.Harvester;
import org.n52.socialmedia.StringUtil;
import org.n52.socialmedia.harvester.storage.InsertObservationToSOS;
import org.n52.socialmedia.harvester.storage.ObservationStorage;
import org.n52.socialmedia.harvester.storage.TemplateUtil;
import org.n52.socialmedia.model.HumanVisualPerceptionObservation;
import org.n52.socialmedia.model.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HarvesterController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(HarvesterController.class);
	
	private List<Harvester> harvesterInstances = new ArrayList<>();
	private ObservationStorage storage;
	
	public HarvesterController() {
		ServiceLoader<Harvester> services = ServiceLoader.load(Harvester.class);
		
		for (Harvester harvester : services) {
			this.harvesterInstances.add(harvester);
		}
		
		this.storage = new InsertObservationToSOS();
	}
	
	public void harvest() throws DecodingException, IOException {
		
		List<Location> locations = new ArrayList<Location>();
		locations.add(new LocationImpl(37.782595, -122.397274));
		locations.add(new LocationImpl(37.782595, -122.441219));
		locations.add(new LocationImpl(37.782595, -122.482418));
		
		locations.add(new LocationImpl(37.755728, -122.397274));
		locations.add(new LocationImpl(37.755728, -122.441219));
		locations.add(new LocationImpl(37.755728, -122.482418));
		
		locations.add(new LocationImpl(37.730751, -122.397274));
		locations.add(new LocationImpl(37.730751, -122.441219));
		locations.add(new LocationImpl(37.730751, -122.482418));
		
		DateTime start = new DateTime("2014-12-10");
		DateTime end = new DateTime("2014-12-12");
		
		StringBuilder sb = new StringBuilder();
		for (Location l : locations) {
			sb.append(TemplateUtil.fillTemplate("/point_feature_template.json", createLatLon(l)));
			sb.append(", ");
		}
		sb.delete(sb.length()-2, sb.length());
		
		Map<String, String> values = new HashMap<>();
		values.put("features", sb.toString());
		String features = TemplateUtil.fillTemplate("/feature_collection_template.json", values);
		LOGGER.info("harvesting features for points: "+features);
		
		for (Harvester harvester : harvesterInstances) {
			for (Location location : locations) {
				Collection<HumanVisualPerceptionObservation> list = harvester.searchForImagesAt(location.getLatitude(), location.getLongitude(), start, end);
				this.storage.storeObservations(list);		
			}
		}
		
	}
	
	private Map<String, String> createLatLon(Location l) {
		Map<String, String> result = new HashMap<>();
		result.put("lon", l.getLongitude().toString());
		result.put("lat", l.getLatitude().toString());
		return result;
	}

	public static void main(String[] args) throws DecodingException, IOException {
		new HarvesterController().harvest();
	}

}
