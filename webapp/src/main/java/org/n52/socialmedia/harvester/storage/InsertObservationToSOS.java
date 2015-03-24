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
package org.n52.socialmedia.harvester.storage;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.n52.instagram.decode.JsonUtil;
import org.n52.socialmedia.model.HumanVisualPerceptionObservation;
import org.n52.socialmedia.model.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InsertObservationToSOS implements ObservationStorage {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(InsertObservationToSOS.class);
	
	private static final DateTimeFormatter ISO_FORMAT = ISODateTimeFormat.dateTimeNoMillis();

	private static final String OBSERVATION_TEMPLATE = "/observation_template.xml";
	private static final String INSERT_OBSERVATION_TEMPLATE = "/insert_observation_template.xml";
	private static final String INSERT_SENSOR_TEMPLATE = "/insert_sensor_template.xml";

	private static final String SOS_URL = "http://ows.dev.52north.org:8080/52n-wfs-webapp/sos/soap";

	@Override
	public void storeObservations(Collection<HumanVisualPerceptionObservation> observations) throws IOException {
		for (HumanVisualPerceptionObservation o : observations) {
			executeInsert(TemplateUtil.fillTemplate(OBSERVATION_TEMPLATE, createValueMap(o)), o.getProcedure());
		}
	}

	private void executeInsert(String obs, String offering) throws IOException {
		checkAndInsertSensor(offering, offering);
		
		Map<String, String> values = new HashMap<>();
		values.put("observation", obs);
		values.put("offering", offering);
		String requestContent = TemplateUtil.fillTemplate(INSERT_OBSERVATION_TEMPLATE, values);

		executePost(requestContent);
	}

	private String executePost(String requestContent) throws IOException {
		HttpPost post = new HttpPost(SOS_URL);
		post.setHeader("Content-Type", "application/soap+xml");
		post.setHeader("Accept", "application/soap+xml");
		post.setEntity(new StringEntity(requestContent));
		
		try (CloseableHttpClient client = HttpClientBuilder.create().build();) {
			CloseableHttpResponse response = client.execute(post);
			
			if (response.getEntity() != null) {
				String content = EntityUtils.toString(response.getEntity());
				LOGGER.info("response: "+content);
				return content;
			}
			else {
				LOGGER.warn("Could not retrieve contents. No entity.");
			}
		} catch (IOException e) {
			LOGGER.warn("Could not retrieve contents of "+post.getURI(), e);
		}
		
		return null;
	}

	private void checkAndInsertSensor(String offering, String procedure) throws IOException {
		Map<String, String> values = new HashMap<>();
		values.put("offering", offering);
		values.put("procedure", procedure);
		
		String template = TemplateUtil.fillTemplate(INSERT_SENSOR_TEMPLATE, values);
		
		executePost(template);
	}

	private Map<String, String> createValueMap(
			HumanVisualPerceptionObservation o) {
		Map<String, String> result = new HashMap<>();
		
		result.put("phenTime", o.getPhenomenonTime().toString(ISO_FORMAT));
		result.put("resultTime", o.getResultTime().toString(ISO_FORMAT));
		result.put("description", o.getDescription());
		result.put("identifier", o.getIdentifier());
		result.put("name", o.getName());
		result.put("procedure", o.getProcedure());
		result.put("resultHref", o.getResultHref());
		result.put("lonLat", Location.Util.toLonLatString(o.getLocation()));
		
		return result;
	}

}
