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
