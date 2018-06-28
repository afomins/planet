/*
 * Planet
 * Copyright (C) 2018 Alex Fomins
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

// -----------------------------------------------------------------------------
package com.matalok.planet;

// -----------------------------------------------------------------------------
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

// -----------------------------------------------------------------------------
public class UtilsGeo {
    // -------------------------------------------------------------------------
    public static float GetLat(Vector3 v) { return v.y; };
    public static float GetLon(Vector3 v) { return v.x; };
    public static float GetLat(Vector2 v) { return v.y; };
    public static float GetLon(Vector2 v) { return v.x; };
    public static float SetLat(Vector3 v, float val) { v.y = val; return v.y; };
    public static float SetLon(Vector3 v, float val) { v.x = val; return v.x; };
    public static float SetLat(Vector2 v, float val) { v.y = val; return v.y; };
    public static float SetLon(Vector2 v, float val) { v.x = val; return v.x; };
    public static Vector2 SetLatLon(Vector2 v, float lat, float lon) { v.y = lat; v.x = lon; return v; };
    public static Vector3 SetLatLon(Vector3 v, float lat, float lon) { v.y = lat; v.x = lon; return v; };

    // -------------------------------------------------------------------------
    // http://www.movable-type.co.uk/scripts/latlong-vectors.html
    // LatLon.prototype.greatCircle
    static private Vector3 great_circle = new Vector3();
    public static Vector3 GetGreatCircle(Vector2 geo, float bearing) {
        float lat = UtilsGeo.GetLat(geo),
              lon = UtilsGeo.GetLon(geo),
              lon_sin = MathUtils.sin(lon),
              lon_cos = MathUtils.cos(lon),
              lat_sin = MathUtils.sin(lat),
              lat_cos = MathUtils.cos(lat),
              bear_sin = MathUtils.sin(bearing),
              bear_cos = MathUtils.cos(bearing);
        return great_circle.set(
             lon_sin * bear_cos - lat_sin * lon_cos * bear_sin,
            -lon_cos * bear_cos - lat_sin * lon_sin * bear_sin,
             lat_cos * bear_sin);
    }

    // -------------------------------------------------------------------------
    // http://www.movable-type.co.uk/scripts/latlong.html
    // LatLon.prototype.distanceTo
    public static float GetDistance(Vector3 geo0, Vector3 geo1) {
        Vector3 diff = Utils.GetDiff(geo0, geo1);
        float lat0 = UtilsGeo.GetLat(geo0),
              lat1 = UtilsGeo.GetLat(geo1),
              diff_lon = UtilsGeo.GetLon(diff),
              diff_lat = UtilsGeo.GetLat(diff),
              diff_lon_sin = MathUtils.sin(diff_lon / 2),
              diff_lat_sin = MathUtils.sin(diff_lat / 2);
        float a = diff_lat_sin * diff_lat_sin +
                  diff_lon_sin * diff_lon_sin *
                  MathUtils.cos(lat0) * MathUtils.cos(lat1);
        return 2 * MathUtils.atan2((float)Math.sqrt(a), (float)Math.sqrt(1.0f - a));
    }

    // -------------------------------------------------------------------------
    // http://www.movable-type.co.uk/scripts/latlong.html
    // LatLon.prototype.destinationPoint
    public static Vector3 GetDest(Vector3 dest, Vector3 start, float distance, float bearing, float radius) {
        // Angular distance in radians
        distance /= radius;

        float lat = UtilsGeo.GetLat(start),
              lon = UtilsGeo.GetLon(start),
              lat_sin = MathUtils.sin(lat),
              lat_cos = MathUtils.cos(lat),
//              lon_sin = MathUtils.sin(lon),
//              lon_cos = MathUtils.cos(lon),
              dist_sin = MathUtils.sin(distance),
              dist_cos = MathUtils.cos(distance),
              bear_sin = MathUtils.sin(bearing),
              bear_cos = MathUtils.cos(bearing);

        float lat_dest = (float)Math.asin(
            lat_sin * dist_cos +
            lat_cos * dist_sin * bear_cos);

        float lon_dest = lon + MathUtils.atan2(
            bear_sin * dist_sin * lat_cos, 
            dist_cos - lat_sin * MathUtils.sin(lat_dest));

        // Normalize longitude to -180..+180
        lon_dest = (lon_dest + 3 * MathUtils.PI) % (2 * MathUtils.PI) - MathUtils.PI;

        // Write destination
        return UtilsGeo.SetLatLon(dest, lat_dest, lon_dest);
    }

    // -------------------------------------------------------------------------
    // http://www.movable-type.co.uk/scripts/latlong.html
    // LatLon.prototype.bearingTo
    public static float GetBearing(Vector3 geo0, Vector3 geo1) {
        float lat0 = UtilsGeo.GetLat(geo0),
              lon0 = UtilsGeo.GetLon(geo0),
              lat1 = UtilsGeo.GetLat(geo1),
              lon1 = UtilsGeo.GetLon(geo1),
              lon_delta = lon1 - lon0;

        float y = MathUtils.sin(lon_delta) * MathUtils.cos(lat1),
              x = MathUtils.cos(lat0) * MathUtils.sin(lat1) - 
                  MathUtils.sin(lat0) * MathUtils.cos(lat1) * MathUtils.cos(lon_delta);

        return (MathUtils.atan2(y, x) + MathUtils.PI2) % MathUtils.PI2;
    }

    // -------------------------------------------------------------------------
    private static Vector3 bearing_from = new Vector3(),
                           bearing_to = new Vector3();
    public static float GetBearingByCartesian(Vector3 v0, Vector3 v1) {
        return UtilsGeo.GetBearing(
            UtilsGeo.CartesianToGeo(bearing_from, v0.x, v0.y, v0.z), 
            UtilsGeo.CartesianToGeo(bearing_to, v1.x, v1.y, v1.z));
    }

    // -------------------------------------------------------------------------
    public static float GetBearingByCartesian(Vector3 v, Vector3 dir, float dir_scale) {
        return UtilsGeo.GetBearingByCartesian(
           v,                                               // From 
           bearing_to.set(dir).scl(dir_scale).add(v));      // To
    }

    // -------------------------------------------------------------------------
    // http://www.movable-type.co.uk/scripts/latlong-vectors.html
    // From latitude/longitude to n-vector
    public static Vector3 GeoToCartesian(Vector3 dest, float lon, float lat, float radius) {
        float cos_lat_r = MathUtils.cos(lat) * radius,
              sin_lat_r = MathUtils.sin(lat) * radius,
              cos_lon = MathUtils.cos(lon),
              sin_lon = MathUtils.sin(lon);
        return dest.set(cos_lat_r * cos_lon, sin_lat_r, cos_lat_r * sin_lon);
    }

    // -------------------------------------------------------------------------
    // http://www.movable-type.co.uk/scripts/latlong-vectors.html
    // From n-vector to latitude/longitude
    public static Vector3 CartesianToGeo(Vector3 dest, float x, float y, float z) {
        return UtilsGeo.SetLatLon(dest, 
            MathUtils.atan2(y, (float)Math.sqrt(x * x + z * z)), 
            MathUtils.atan2(z, x));
    }

    // -------------------------------------------------------------------------
    public static Vector2 GeoToSpheric(Vector2 dest, float lon, float lat, boolean target_north) {
        Utils.Assert(lon >= 0.0f && lon <= MathUtils.PI2, 
            "Failed to convert geo to spheric, wrong longitude :: lon=%.2f", lon);
        Utils.Assert(lat >= -MathUtils.PI / 2 && lat <= MathUtils.PI / 2, 
            "Failed to convert geo to spheric, wrong latitude :: lat=%.2f", lat);

        // Force positive latitude
        boolean is_north = true;
        if(lat < 0.0f) {
            lat = -lat;
            is_north = false;
        }

        // Get radius from circle center to coordinate
        float radius = (is_north == target_north) ?  
            1.0f - MathUtils.sin(lat) : // Coordinate in target hemisphere
            1.0f + MathUtils.sin(lat);  // Coordinate in opposite hemisphere

        // Return spheric coordinates with following vector length meaning:
        //  *** [0.0f; 1.0f] - coordinate in target hemisphere
        //  *** [1.0f; 2.0f] - coordinate in opposite hemisphere
        return dest.set(
            MathUtils.cos(lon), 
            MathUtils.sin(lon)).scl(radius);
    }

    // -------------------------------------------------------------------------
    public static Vector2 SphericToGeo(Vector2 coord, boolean target_north) {
        // Get normalized spheric radius [-1.0f; +1.0f]
        float radius = 1.0f - coord.len();

        // South hemisphere has inverted latitude value
        if(!target_north) {
            radius = -radius;
        }

        // Normalize coordinates
        coord.nor();

        // Convert to geo
        return UtilsGeo.SetLatLon(coord, 
            (float)Math.asin(radius), 
            MathUtils.atan2(coord.y, coord.x));
    }
}
