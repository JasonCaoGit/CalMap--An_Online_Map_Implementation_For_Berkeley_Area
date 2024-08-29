package bearmaps.proj2c.server.handler.impl;

import bearmaps.proj2c.AugmentedStreetMapGraph;
import bearmaps.proj2c.server.handler.APIRouteHandler;
import spark.Request;
import spark.Response;
import bearmaps.proj2c.utils.Constants;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

import static bearmaps.proj2c.utils.Constants.*;

/**
 * Handles requests from the web browser for map images. These images
 * will be rastered into one large image to be displayed to the user.
 * @author rahul, Josh Hug, _________
 */
public class RasterAPIHandler extends APIRouteHandler<Map<String, Double>, Map<String, Object>> {

    /**
     * Each raster request to the server will have the following parameters
     * as keys in the params map accessible by,
     * i.e., params.get("ullat") inside RasterAPIHandler.processRequest(). <br>
     * ullat : upper left corner latitude, <br> ullon : upper left corner longitude, <br>
     * lrlat : lo0  1wer right corner latitude,<br> lrlon : lower right corner longitude <br>
     * w : user viewport window width in pixels,<br> h : user viewport height in pixels.
     **/
    private static final String[] REQUIRED_RASTER_REQUEST_PARAMS = {"ullat", "ullon", "lrlat",
            "lrlon", "w", "h"};

        private static final double WIDTH = Constants.ROOT_LRLON - Constants.ROOT_ULLON;
    private static final double HEIGHT = Constants.ROOT_ULLAT - Constants.ROOT_LRLAT;

    /**
     * The result of rastering must be a map containing all of the
     * fields listed in the comments for RasterAPIHandler.processRequest.
     **/
    private static final String[] REQUIRED_RASTER_RESULT_PARAMS = {"render_grid", "raster_ul_lon",
            "raster_ul_lat", "raster_lr_lon", "raster_lr_lat", "depth", "query_success"};

    public static final ArrayList<Double> LONGDPPS = findLongDPPs();

    public static void main(String[] args) {

        RasterAPIHandler apiHandler = new RasterAPIHandler();
        Map<String, Double> params = new HashMap<>();


        params.put("ullat", 37.877213453652814);
        params.put("ullon", -122.26859396699524);
        params.put("lrlat", 37.86643854634718);
        params.put("lrlon", -122.25157803300476);
        params.put("w", 793.0);
        params.put("h", 636.0);

        apiHandler.processRequest(params);

    }

    /**
     * The result of rastering must be a map containing all of the
     * fields listed in the comments for RasterAPIHandler.processRequest.
     **/



    @Override
    protected Map<String, Double> parseRequestParams(Request request) {
        return getRequestParams(request, REQUIRED_RASTER_REQUEST_PARAMS);
    }

    /**
     * Takes a user query and finds the grid of images that best matches the query. These
     * images will be combined into one big image (rastered) by the front end. <br>
     *
     *     The grid of images must obey the following properties, where image in the
     *     grid is referred to as a "tile".
     *     <ul>
     *         <li>The tiles collected must cover the most longitudinal distance per pixel
     *         (LonDPP) possible, while still covering less than or equal to the amount of
     *         longitudinal distance per pixel in the query box for the user viewport size. </li>
     *         <li>Contains all tiles that intersect the query bounding box that fulfill the
     *         above condition.</li>
     *         <li>The tiles must be arranged in-order to reconstruct the full image.</li>
     *     </ul>
     *

     * @return A map of results for the front end as specified: <br>
     * "render_grid"   : String[][], the files to display. <br>
     * "raster_ul_lon" : Number, the bounding upper left longitude of the rastered image. <br>
     * "raster_ul_lat" : Number, the bounding upper left latitude of the rastered image. <br>
     * "raster_lr_lon" : Number, the bounding lower right longitude of the rastered image. <br>
     * "raster_lr_lat" : Number, the bounding lower right latitude of the rastered image. <br>
     * "depth"         : Number, the depth of the nodes of the rastered image;
     *                    can also be interpreted as the length of the numbers in the image
     *                    string. <br>
     * "query_success" : Boolean, whether the query was able to successfully complete; don't
     *                    forget to set this to true on success! <br>
     */


    @Override
    protected Object buildJsonResponse(Map<String, Object> result) {
        boolean rasterSuccess = validateRasteredImgParams(result);

        if (rasterSuccess) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            writeImagesToOutputStream(result, os);
            String encodedImage = Base64.getEncoder().encodeToString(os.toByteArray());
            result.put("b64_encoded_image_data", encodedImage);
        }
        return super.buildJsonResponse(result);
    }


        public Map<String, Object> processRequest(Map<String, Double> requestParams) {
//        System.out.println("yo, wanna know the parameters given by the web browser? They are:");
//        System.out.println(requestParams);
//        System.out.println("Since you haven't implemented RasterAPIHandler.processRequest, nothing is displayed in "
//                + "your browser.");
        Map<String, Object> results = new HashMap<>();
        String[][] renderGrid;
        double rasterULLon; // upper left x
        double rasterULLat; // upper left y
        double rasterLRLon; // lower right x
        double rasterLRLat; // lower right y
        int depth;
        boolean querySuccess = true;

        // Parse the request parameters.
        double requestULLon = requestParams.get("ullon");
        double requestULLat = requestParams.get("ullat");
        double requestLRLon = requestParams.get("lrlon");
        double requestLRLat = requestParams.get("lrlat");
        double requestWidth = requestParams.get("w");
        double requestHeight = requestParams.get("h");

        // Request grid is completely out of the whole image.
        if (Double.compare(requestULLon, ROOT_LRLON) > 0
                || Double.compare(requestULLat, ROOT_LRLAT) < 0
                || Double.compare(requestLRLon, ROOT_ULLON) < 0
                || Double.compare(requestLRLat, ROOT_ULLAT) > 0) {
            querySuccess = false;
        }

        // Nonsense request grid.
        if (Double.compare(requestULLon, requestLRLon) > 0
                || Double.compare(requestULLat, requestLRLat) < 0) {
            querySuccess = false;
        }

        results.put("query_success", querySuccess);

        // Find the optimal depth.
        double requestLonDPP = (requestLRLon - requestULLon) / requestWidth;
        depth = calcOptimalDepth(requestLonDPP);
        results.put("depth", depth);

        // Find the rastered parameters.
        int rasterULLonNum = calcRasteredParamNum(depth, requestULLon, ROOT_ULLON, ROOT_LRLON, true);
        rasterULLon = calcRasteredParam(depth, rasterULLonNum, ROOT_ULLON, ROOT_LRLON, true ,true);
        results.put("raster_ul_lon", rasterULLon);
        System.out.println("raster_ul_lonr" + rasterULLon);

        int rasterULLatNum = calcRasteredParamNum(depth, requestULLat, ROOT_ULLAT, ROOT_LRLAT, true);
        rasterULLat = calcRasteredParam(depth, rasterULLatNum, ROOT_ULLAT, ROOT_LRLAT, true, false);
        results.put("raster_ul_lat", rasterULLat);
         System.out.println("raster_ul_latr"+ rasterULLat);

        int rasterLRLonNum = calcRasteredParamNum(depth, requestLRLon, ROOT_ULLON, ROOT_LRLON, false);
        rasterLRLon = calcRasteredParam(depth, rasterLRLonNum, ROOT_ULLON, ROOT_LRLON, false, true);
        results.put("raster_lr_lon", rasterLRLon);
        System.out.println("raster_lr_lonr"+ rasterLRLon);

        int rasterLRLatNum = calcRasteredParamNum(depth, requestLRLat, ROOT_ULLAT, ROOT_LRLAT, false);
        rasterLRLat = calcRasteredParam(depth, rasterLRLatNum, ROOT_ULLAT, ROOT_LRLAT, false, false);
        results.put("raster_lr_lat", rasterLRLat);
        System.out.println("raster_lr_latr" + rasterLRLat);

        // Add 1 here because the LRNum is underestimated 1 in the previous
        // calcRasteredParamNum in order to correspond to image number.
        int rowNum = rasterLRLatNum - rasterULLatNum + 1;
        int colNum = rasterLRLonNum - rasterULLonNum + 1;
        renderGrid = new String[rowNum][colNum];
        for (int i = 0; i < rowNum; i += 1) {
            for (int j = 0; j < colNum; j += 1) {
                renderGrid[i][j] = "d" + depth + "_x" + (j + rasterULLonNum) + "_y" + (i + rasterULLatNum) + ".png";
            }
        }

        results.put("render_grid", renderGrid);
         for (int i = 0; i < rowNum; i += 1) {
            for (int j = 0; j < colNum; j += 1) {
                System.out.println(renderGrid[i][j]);
            }
        }



        return results;
    }


//    public Map<String, Object> processRequest(Map<String, Double> requestParams, Response response) {
////        System.out.println("yo, wanna know the parameters given by the web browser? They are:");
////        System.out.println(requestParams);
////        System.out.println("Since you haven't implemented RasterAPIHandler.processRequest, nothing is displayed in "
////                + "your browser.");
//        Map<String, Object> results = new HashMap<>();
//        String[][] renderGrid;
//        double rasterULLon; // upper left x
//        double rasterULLat; // upper left y
//        double rasterLRLon; // lower right x
//        double rasterLRLat; // lower right y
//        int depth;
//        boolean querySuccess = true;
//
//        // Parse the request parameters.
//        double requestULLon = requestParams.get("ullon");
//        double requestULLat = requestParams.get("ullat");
//        double requestLRLon = requestParams.get("lrlon");
//        double requestLRLat = requestParams.get("lrlat");
//        double requestWidth = requestParams.get("w");
//        double requestHeight = requestParams.get("h");
//
//        // Request grid is completely out of the whole image.
//        if (Double.compare(requestULLon, ROOT_LRLON) > 0
//                || Double.compare(requestULLat, ROOT_LRLAT) < 0
//                || Double.compare(requestLRLon, ROOT_ULLON) < 0
//                || Double.compare(requestLRLat, ROOT_ULLAT) > 0) {
//            querySuccess = false;
//        }
//
//        // Nonsense request grid.
//        if (Double.compare(requestULLon, requestLRLon) > 0
//                || Double.compare(requestULLat, requestLRLat) < 0) {
//            querySuccess = false;
//        }
//
//        results.put("query_success", querySuccess);
//
//        // Find the optimal depth.
//        double requestLonDPP = (requestLRLon - requestULLon) / requestWidth;
//        depth = calcOptimalDepth(requestLonDPP);
//        results.put("depth", depth);
//
//        // Find the rastered parameters.
//        int rasterULLonNum = calcRasteredParamNum(depth, requestULLon, ROOT_ULLON, ROOT_LRLON, true);
//        rasterULLon = calcRasteredParam(depth, rasterULLonNum, ROOT_ULLON, ROOT_LRLON, true ,true);
//        results.put("raster_ul_lon", rasterULLon);
//        System.out.println("raster_ul_lonr" + rasterULLon);
//
//        int rasterULLatNum = calcRasteredParamNum(depth, requestULLat, ROOT_ULLAT, ROOT_LRLAT, true);
//        rasterULLat = calcRasteredParam(depth, rasterULLatNum, ROOT_ULLAT, ROOT_LRLAT, true, false);
//        results.put("raster_ul_lat", rasterULLat);
//         System.out.println("raster_ul_latr"+ rasterULLat);
//
//        int rasterLRLonNum = calcRasteredParamNum(depth, requestLRLon, ROOT_ULLON, ROOT_LRLON, false);
//        rasterLRLon = calcRasteredParam(depth, rasterLRLonNum, ROOT_ULLON, ROOT_LRLON, false, true);
//        results.put("raster_lr_lon", rasterLRLon);
//        System.out.println("raster_lr_lonr"+ rasterLRLon);
//
//        int rasterLRLatNum = calcRasteredParamNum(depth, requestLRLat, ROOT_ULLAT, ROOT_LRLAT, false);
//        rasterLRLat = calcRasteredParam(depth, rasterLRLatNum, ROOT_ULLAT, ROOT_LRLAT, false, false);
//        results.put("raster_lr_lat", rasterLRLat);
//        System.out.println("raster_lr_latr" + rasterLRLat);
//
//        // Add 1 here because the LRNum is underestimated 1 in the previous
//        // calcRasteredParamNum in order to correspond to image number.
//        int rowNum = rasterLRLatNum - rasterULLatNum + 1;
//        int colNum = rasterLRLonNum - rasterULLonNum + 1;
//        renderGrid = new String[rowNum][colNum];
//        for (int i = 0; i < rowNum; i += 1) {
//            for (int j = 0; j < colNum; j += 1) {
//                renderGrid[i][j] = "d" + depth + "_x" + (j + rasterULLonNum) + "_y" + (i + rasterULLatNum) + ".png";
//            }
//        }
//
//        results.put("render_grid", renderGrid);
//         for (int i = 0; i < rowNum; i += 1) {
//            for (int j = 0; j < colNum; j += 1) {
//                System.out.println(renderGrid[i][j]);
//            }
//        }
//
//
//
//        return results;
//    }

    /**
     * Calculate the optimal depth using the LonDPP of the request.
     */
    private int calcOptimalDepth(double requestLonDPP) {
        double baseLonDPP = (ROOT_LRLON - ROOT_ULLON) / TILE_SIZE;

        // Take the log of base 0.5 to get the optimal depth.
        // Always round up to fulfill the LonDPP requirement.
        // Use the log change-of-base formula.
        int optimalDepth = (int) Math.ceil((Math.log(requestLonDPP / baseLonDPP) / Math.log(0.5)));
        if (optimalDepth < 7) {
            return optimalDepth;
        }

        // Since the deepest depth is 7.
        return 7;
    }

    /**
     * Calculate the corresponding grid number of the rastered parameter.
     */
    private int calcRasteredParamNum(int depth, double requestParam, double rootUL, double rootLR, boolean isUL) {
        int bound = (int) (Math.pow(2, depth) - 1);

        // Calculate the specific size (width or height) of each tile in current depth.
        double tileSize = Math.abs(rootUL - rootLR) / (bound + 1);

        // Always find the difference relative to the root upper left corner.
        double temp = Math.abs(requestParam - rootUL) / tileSize;

        // Round down the temp, because the number of image counting from zero.
        int rasteredParamNum = (int) Math.floor(temp);

        // If num larger than the bound num, set it to bound num.
        // If num smaller than 0, set it to 0;
        if (rasteredParamNum > bound) {
            rasteredParamNum = bound;
        } else if (rasteredParamNum < 0) {
            rasteredParamNum = 0;
        }

        return rasteredParamNum;
    }

    /**
     * Calculate the rastered parameter.
     */
    private double calcRasteredParam(int depth, int rasteredParamNum, double rootUL, double rootLR, boolean isUL, boolean isLon) {
        int bound = (int) (Math.pow(2, depth) - 1);

        // Calculate the specific size (width or height) of each tile in current depth.
        double tileSize = Math.abs(rootUL - rootLR) / (bound + 1);
        double rasteredParam;

        // Always add(minus) from the upper(left) corner's Lon or Lat.
        if (isUL) {
            if (isLon) {
                rasteredParam = rootUL + rasteredParamNum * tileSize;
            } else {
                rasteredParam = rootUL - rasteredParamNum * tileSize;
            }

            // If calculate lower right coordinates, the rasteredParamNum
            // needs to plus 1, because for each tile, the right(lower) side
            // is 1 tileSize away from the left(upper) side.
        } else {
            if (isLon) {
                rasteredParam = rootUL + (rasteredParamNum + 1) * tileSize;
            } else {
                rasteredParam = rootUL - (rasteredParamNum + 1) * tileSize;
            }
        }

        return rasteredParam;
    }



      public static ArrayList<Double> findLongDPPs() {
        double width = Constants.ROOT_LRLON - Constants.ROOT_ULLON;
        double height = Constants.ROOT_ULLON - Constants.ROOT_LRLAT;
        //LongDPP = width/2 to the ith power / 256
        ArrayList<Double> longDPPs = new ArrayList<>();

        for (int i = 0; i < 8; i++) {
            double longDPP = width / Math.pow(2, i) / 256;
            longDPPs.add(longDPP);

        }
        return longDPPs;


    }



    public static int findRightDepth(double longDPP) {
        //The images of the same depth will not change its longDPP once put together
        //iterate through longdpps find the first longdpp that is smaller or equal than
        //return the index
        for (int i = 0; i < LONGDPPS.size(); i++) {
            if (LONGDPPS.get(i) < longDPP) {
                return i -1 ;
            }
        }
        //RETURN the most clear no matter what

        return LONGDPPS.size() - 1;
    }


    public static int findLowerXIndex(double tileWidth, double ullon, int numOfSideTiles) {

        double xScale = Constants.ROOT_ULLON;
        for (int i = 0; i < numOfSideTiles; i += 1) {
            xScale += tileWidth;
            if (ullon < xScale) {
                return i;
            }

        }
        return -1;

    }

    public static int findUpperXIndex(double tileWidth, double lrlon, int numOfSideTiles) {

        double xScale = Constants.ROOT_LRLON;

        for (int i = numOfSideTiles -1 ; i >= 0; i -= 1) {
            xScale -= tileWidth;
            if (xScale < lrlon) {

                return i;
            }

        }
        return -1;

    }

    public static int findLowerYIndex(double tileWidth, double ullat, int numOfSideTiles) {
        double yScale = Constants.ROOT_ULLAT;

        for (int i = 0; i < numOfSideTiles; i += 1) {
            yScale -= tileWidth;
            if (yScale < ullat) {

                return i;
            }

        }
        return -1;

    }


    public static int findUpperYIndex(double tileWidth, double lrlat, int numOfSideTiles) {
        double yScale = Constants.ROOT_LRLAT;

        for (int i = numOfSideTiles -1 ; i >= 0; i -= 1) {
            yScale += tileWidth;
            if (lrlat < yScale) {
                return i;
            }

        }
        return -1;


    }







    /**
     * Takes a user query and finds the grid of images that best matches the query. These
     * images will be combined into one big image (rastered) by the front end. <br>
     *
     *     The grid of images must obey the following properties, where image in the
     *     grid is referred to as a "tile".
     *     <ul>
     *         <li>The tiles collected must cover the most longitudinal distance per pixel
     *         (LonDPP) possible, while still covering less than or equal to the amount of
     *         longitudinal distance per pixel in the query box for the user viewport size. </li>
     *         <li>Contains all tiles that intersect the query bounding box that fulfill the
     *         above condition.</li>
     *         <li>The tiles must be arranged in-order to reconstruct the full image.</li>
     *     </ul>
     *

     * @return A map of results for the front end as specified: <br>
     * "render_grid"   : String[][], the files to display. <br>
     * "raster_ul_lon" : Number, the bounding upper left longitude of the rastered image. <br>
     * "raster_ul_lat" : Number, the bounding upper left latitude of the rastered image. <br>
     * "raster_lr_lon" : Number, the bounding lower right longitude of the rastered image. <br>
     * "raster_lr_lat" : Number, the bounding lower right latitude of the rastered image. <br>
     * "depth"         : Number, the depth of the nodes of the rastered image;
     *                    can also be interpreted as the length of the numbers in the image
     *                    string. <br>
     * "query_success" : Boolean, whether the query was able to successfully complete; don't
     *                    forget to set this to true on success! <br>
     */




    public Map<String, Object> badQuery() {
        Map<String, Object> results = new HashMap<>();
        results.put("query_success", false);
        results.put("raster_ul_lon", 0);
        results.put("depth", 0);
        results.put("raster_lr_lon", 0);
        results.put("raster_lr_lat", 0);
        String[][] renderGrid = new String[3][4];
        results.put("render_grid", renderGrid);
        results.put("raster_ul_lat", 0);
        return results;

    }




    public Map<String, Object> processRequest(Map<String, Double> requestParams, Response response) {


        Map<String, Object> results = new HashMap<>();




//        {"render_grid", "raster_ul_lon",
//                "raster_ul_lat", "raster_lr_lon", "raster_lr_lat", "depth", "query_success"};
        //First task fill out String[][] render_grid
        /*
         * Find the files that together they will cover all region of the query box
         * 2. Have the greates LonDPP
         *
         * */

      /*  private static final String[] REQUIRED_RASTER_REQUEST_PARAMS = {"ullat", "ullon", "lrlat",
                "lrlon", "w", "h"};
*/
        //find the LonDPP of the user request
        double lrLon = requestParams.get("lrlon");
        double ulLon= requestParams.get("ullon");
        double lrLat = requestParams.get("lrlat");
        double ulLat = requestParams.get("ullat");
        double lonDPP = (lrLon - ulLon) / requestParams.get("w");




         //query box for a location out of the root long and lat
        //raster ul lon set to arbitrry
        //query success false

        //if lower right long is at the left of root ul left
        if (lrLon < ROOT_ULLON || ulLon > ROOT_LRLON || lrLat > ROOT_ULLAT || ulLat < ROOT_LRLAT || ulLon > lrLon || ulLat < lrLat) {

            results = badQuery();
            return results;
        }



        //if ullon,ullat is on the right of lrlon, lrlat
        //query success false



        //Find a depth that has the correct lonDPP, which is just smaller than or equal to the given lonDPP
        //Put all depths in a list, iterate through to find the first that is smaller than or equal to the given
        int optimalDepth = findRightDepth(lonDPP);
        int numOfPics = (int) Math.pow(4, optimalDepth);

        //First find the how many longitudes a tile posses,
        //Get the ul long find the smallest bound that is larger than the ullong,
        // Use index i to get the starting x tile,
        //Length of a grid
        int numOfSideTiles = (int) Math.pow(2, optimalDepth);
        double tileWidth = WIDTH/numOfSideTiles;
        double tileHeight = HEIGHT / numOfSideTiles;


        int lowerXIndex = findLowerXIndex(tileWidth, ulLon, numOfSideTiles);

        int upperXIndex = findUpperXIndex(tileWidth, lrLon, numOfSideTiles);

        int lowerYIndex = findLowerYIndex(tileHeight, ulLat, numOfSideTiles);

        int upperYIndex = findUpperYIndex(tileHeight, lrLat, numOfSideTiles);



        //        {"render_grid", "raster_ul_lon",
//                "raster_ul_lat", "raster_lr_lon", "raster_lr_lat", "depth", "query_success"};
        //find the ul_lon, you use the lowerXIndex
        double ul_lon = findUllon(tileWidth, numOfSideTiles, lowerXIndex);
        System.out.println("ul_lon:" + ul_lon);

        double lr_lon = findLrlon(tileWidth, numOfSideTiles, upperXIndex);
        System.out.println("lr_lon" + lr_lon);

        double ul_lat = findUllat(tileHeight, numOfSideTiles, lowerYIndex);
        System.out.println("ul_lat" + ul_lat);
        double lr_lat = findLrlat(tileHeight, numOfSideTiles, upperYIndex);
        System.out.println("lr_lat"+lr_lat);

        //we have the j, k value for da_xj_yk, now put the filenames into the array
        int numOfX = upperXIndex - lowerXIndex + 1;
        int numOfY = upperYIndex - lowerYIndex + 1;
       /* d7_x84_y28.png*/
        //add file string to the grid
        String[][] renderGrid = new String[numOfY][numOfX];
        for(int y =0; y <numOfY; y++) {
            int yIndex = lowerYIndex + y;
            for(int x = 0; x < numOfX; x++) {
                int xIndex = lowerXIndex + x;

                String file = "d" + optimalDepth+"_x"+xIndex +"_y"+yIndex +".png";
                renderGrid[y][x] = file;


            }
        }
        for(int y =0; y <numOfY; y++) {

            for(int x = 0; x < numOfX; x++) {
                System.out.print(renderGrid[y][x]);


            }
            System.out.println();
        }

        System.out.println(Arrays.toString(renderGrid));
/*        A map of results for the front end as specified: <br>
     * "render_grid"   : String[][], the files to display. <br>
     * "raster_ul_lon" : Number, the bounding upper left longitude of the rastered image. <br>
     * "raster_ul_lat" : Number, the bounding upper left latitude of the rastered image. <br>
     * "raster_lr_lon" : Number, the bounding lower right longitude of the rastered image. <br>
     * "raster_lr_lat" : Number, the bounding lower right latitude of the rastered image. <br>
     * "depth"         : Number, the depth of the nodes of the rastered image;
     *                    can also be interpreted as the length of the numbers in the image
     *                    string. <br>
     * "query_success" : Boolean, whether the query was able to successfully complete; don't
     *                    forget to set this to true on success! <br>*/
        /*{raster_ul_lon=-122.24212646484375, depth=7, raster_lr_lon=-122.24006652832031,
        raster_lr_lat=37.87538940251607,
        render_grid=[[d7_x84_y28.png, d7_x85_y28.png, d7_x86_y28.png], [d7_x84_y29.png, d7_x85_y29.png, d7_x86_y29.png], [d7_x84_y30.png, d7_x85_y30.png, d7_x86_y30.png]],
         raster_ul_lat=37.87701580361881, query_success=true}*/
        results.put("query_success", true);
        results.put("raster_ul_lon", ul_lon);
        results.put("depth", optimalDepth);
        results.put("raster_lr_lon", lr_lon);
        results.put("raster_lr_lat", lr_lat);
        results.put("render_grid", renderGrid);
        results.put("raster_ul_lat", ul_lat);


        System.out.println(results);








        return results;
    }

    //the ullon will be the ullon of the picture that has the lowerXIndex which sits at the leftmost area
    //

    public double findUllon(double tileWidth, int numOfSideTiles, int lowerXIndex) {
        double overAllUllon = Constants.ROOT_ULLON;
        double currentUllon = overAllUllon + tileWidth * lowerXIndex;
        return currentUllon;


    }
    //the lrlon equal to the root left longitude + width *(upperindex +1)
    public double findLrlon(double tileWidth, int numOfSideTiles, int upperXIndex) {
        double overAllLrlon = Constants.ROOT_ULLON;
        double cuurentLrlon = overAllLrlon + tileWidth * (upperXIndex + 1);
        return cuurentLrlon;
    }

    //

    public double findUllat(double tileHeight, int numOfSideTiles, int lowerYIndex) {
        double overAllUllat = Constants.ROOT_ULLAT;
        double currentUllat = overAllUllat - tileHeight * lowerYIndex;
        return currentUllat;
    }

    public double findLrlat(double tileHeight, int numOfSideTiles, int upperYIndex) {
        double overAllUllat = Constants.ROOT_ULLAT;
        double currentLrlat = overAllUllat - tileHeight * (upperYIndex + 1);
        return currentLrlat;
    }






    private Map<String, Object> queryFail() {
        Map<String, Object> results = new HashMap<>();
        results.put("render_grid", null);
        results.put("raster_ul_lon", 0);
        results.put("raster_ul_lat", 0);
        results.put("raster_lr_lon", 0);
        results.put("raster_lr_lat", 0);
        results.put("depth", 0);
        results.put("query_success", false);
        return results;
    }

    /**
     * Validates that Rasterer has returned a result that can be rendered.
     * @param rip : Parameters provided by the rasterer
     */
    private boolean validateRasteredImgParams(Map<String, Object> rip) {
        for (String p : REQUIRED_RASTER_RESULT_PARAMS) {
            if (!rip.containsKey(p)) {
                System.out.println("Your rastering result is missing the " + p + " field.");
                return false;
            }
        }
        if (rip.containsKey("query_success")) {
            boolean success = (boolean) rip.get("query_success");
            if (!success) {
                System.out.println("query_success was reported as a failure");
                return false;
            }
        }
        return true;
    }

    /**
     * Writes the images corresponding to rasteredImgParams to the output stream.
     * In Spring 2016, students had to do this on their own, but in 2017,
     * we made this into provided code since it was just a bit too low level.
     */
    private  void writeImagesToOutputStream(Map<String, Object> rasteredImageParams,
                                                  ByteArrayOutputStream os) {
        String[][] renderGrid = (String[][]) rasteredImageParams.get("render_grid");
        int numVertTiles = renderGrid.length;
        int numHorizTiles = renderGrid[0].length;

        BufferedImage img = new BufferedImage(numHorizTiles * Constants.TILE_SIZE,
                numVertTiles * Constants.TILE_SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics graphic = img.getGraphics();
        int x = 0, y = 0;

        for (int r = 0; r < numVertTiles; r += 1) {
            for (int c = 0; c < numHorizTiles; c += 1) {
                graphic.drawImage(getImage(Constants.IMG_ROOT + renderGrid[r][c]), x, y, null);
                x += Constants.TILE_SIZE;
                if (x >= img.getWidth()) {
                    x = 0;
                    y += Constants.TILE_SIZE;
                }
            }
        }

        /* If there is a route, draw it. */
        double ullon = (double) rasteredImageParams.get("raster_ul_lon"); //tiles.get(0).ulp;
        double ullat = (double) rasteredImageParams.get("raster_ul_lat"); //tiles.get(0).ulp;
        double lrlon = (double) rasteredImageParams.get("raster_lr_lon"); //tiles.get(0).ulp;
        double lrlat = (double) rasteredImageParams.get("raster_lr_lat"); //tiles.get(0).ulp;

        final double wdpp = (lrlon - ullon) / img.getWidth();
        final double hdpp = (ullat - lrlat) / img.getHeight();
        AugmentedStreetMapGraph graph = SEMANTIC_STREET_GRAPH;
        List<Long> route = ROUTE_LIST;

        if (route != null && !route.isEmpty()) {
            Graphics2D g2d = (Graphics2D) graphic;
            g2d.setColor(Constants.ROUTE_STROKE_COLOR);
            g2d.setStroke(new BasicStroke(Constants.ROUTE_STROKE_WIDTH_PX,
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            route.stream().reduce((v, w) -> {
                g2d.drawLine((int) ((graph.lon(v) - ullon) * (1 / wdpp)),
                        (int) ((ullat - graph.lat(v)) * (1 / hdpp)),
                        (int) ((graph.lon(w) - ullon) * (1 / wdpp)),
                        (int) ((ullat - graph.lat(w)) * (1 / hdpp)));
                return w;
            });
        }

        rasteredImageParams.put("raster_width", img.getWidth());
        rasteredImageParams.put("raster_height", img.getHeight());

        try {
            ImageIO.write(img, "png", os);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private BufferedImage getImage(String imgPath) {
        BufferedImage tileImg = null;
        if (tileImg == null) {
            try {
                File in = new File(imgPath);
                tileImg = ImageIO.read(in);
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
            }
        }
        return tileImg;
    }
}
