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
 * license version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.socialmedia.util;

public class CoordinateUtil {

	/*
	 * http://en.wikipedia.org/wiki/Earth_radius#Equatorial_radius
	 */
	private static double EQUATOR_RADIUS = 6378137d;

	/**
	 * @param latitude in degree
	 * @param longitude in degree
	 * @param distance in meter
	 * @return a quadratical bbox around a point (latitude,longitude) with distance * 2 width:
	 * 	<pre>double[] bbox = {minLon,minLat,maxLon,maxLat};</pred>
	 */
	public static double[] createBBoxCordinates(double latitude, double longitude, double distance) {
		// Algorithm based on http://janmatuschek.de/LatitudeLongitudeBoundingCoordinates
		
		// angular distance in radians on a great circle
		double radDist = distance / EQUATOR_RADIUS ;

		double radLat = Math.toRadians(latitude);
		double radLon = Math.toRadians(longitude);
		double minLat = radLat - radDist;
		double maxLat = radLat + radDist;

		double minLon, maxLon;
		double MIN_LAT = -90.0d;
		double MAX_LAT = 90.0d;
		double MIN_LON = -180.0d;
		double MAX_LON = 180.0d;
		if (minLat > MIN_LAT  && maxLat < MAX_LAT) {
			double deltaLon = Math.asin(Math.sin(radDist) /
				Math.cos(radLat));
			minLon = radLon - deltaLon;
			if (minLon < MIN_LON) minLon += 2.0d * Math.PI;
			maxLon = radLon + deltaLon;
			if (maxLon > MAX_LON) maxLon -= 2.0d * Math.PI;
		} else {
			// a pole is within the distance
			minLat = Math.max(minLat, MIN_LAT);
			maxLat = Math.min(maxLat, MAX_LAT);
			minLon = MIN_LON;
			maxLon = MAX_LON;
		}
		
		double[] bbox = {Math.toDegrees(minLon),
				Math.toDegrees(minLat),
				Math.toDegrees(maxLon),
				Math.toDegrees(maxLat)};
		return bbox;
	}
	
}
