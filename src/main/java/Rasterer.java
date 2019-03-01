import java.util.HashMap;
import java.util.Map;

/**
 * This class provides all code necessary to take a query box and produce
 * a query result. The getMapRaster method must return a Map containing all
 * seven of the required fields, otherwise the front end code will probably
 * not draw the output correctly.
 */
public class Rasterer {
    private double lrlon = -122.2119140625;
    private double lrlat = 37.82280243352756;
    private double ullon = -122.2998046875;
    private double ullat = 37.892195547244356;
    private double width = 256;
    private int depth = -1;

    public Rasterer() {
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
     * @param params Map of the HTTP GET request's query parameters - the query box and
     *               the user viewport width and height.
     *
     * @return A map of results for the front end as specified: <br>
     * "render_grid"   : String[][], the files to display. <br>
     * "raster_ul_lon" : Number, the bounding upper left longitude of the rastered image. <br>
     * "raster_ul_lat" : Number, the bounding upper left latitude of the rastered image. <br>
     * "raster_lr_lon" : Number, the bounding lower right longitude of the rastered image. <br>
     * "raster_lr_lat" : Number, the bounding lower right latitude of the rastered image. <br>
     * "depth"         : Number, the depth of the nodes of the rastered image <br>
     * "query_success" : Boolean, whether the query was able to successfully complete; don't
     *                    forget to set this to true on success! <br>
     */
    public Map<String, Object> getMapRaster(Map<String, Double> params) {

//        for (int i = 0; i < set(params).length; i++) {
//            System.out.println(set(params)[i]);
//        }
        depth = findDepth(params);
        double[] rasterSpecs = rasteredSpecs(params);

        double rasterullon = rasterSpecs[0];
        double rasterullat = rasterSpecs[1];
        double rasterlrlon = rasterSpecs[2];
        double rasterlrlat = rasterSpecs[3];

        String[][] rendergrid = rendergridMethod(params);
        for (int i = 0; i < rendergrid.length; i++) {
            for (int j = 0; j < rendergrid[0].length; j++) {
                System.out.println(rendergrid[i][j]);
            }
        }


        Map<String, Object> results = new HashMap<>();
        results.put("render_grid", rendergrid);
        results.put("raster_ul_lon", rasterullon);
        results.put("raster_ul_lat", rasterullat);
        results.put("raster_lr_lon", rasterlrlon);
        results.put("raster_lr_lat", rasterlrlat);
        results.put("depth", depth);
        results.put("query_success", true);
        return results;
    }

    public double lonDPPmethod(double lRlon, double uLlon, double dwidth) {
        return ((lRlon - uLlon) / dwidth);
    }

    public int findDepth(Map<String, Double> params) {
        double fakeLrlon = lrlon;
        double plondpp = lonDPPmethod(params.get("lrlon"), params.get("ullon"), params.get("w"));
        double londpp = lonDPPmethod(fakeLrlon, ullon, width);
        int ddepth = 0;
        while (londpp > plondpp) {

            fakeLrlon = (fakeLrlon + ullon) / 2;
            londpp = lonDPPmethod(fakeLrlon, ullon, width);
            ddepth++;

        }
        if (ddepth > 7) {
            return 7;
        }
        return ddepth;
    }

    public String[][] rendergridMethod(Map<String, Double> params) {

        int upperX = findUpperX(ullon, tileWidth(lrlon, ullon, depth), params.get("ullon"));
//        System.out.println("upperx: " + upperX);
        int upperY = findUpperY(ullat, tileHeight(lrlat, ullat, depth), params.get("ullat"));
//        System.out.println("uppery" + upperY);
        int lowerX = findLowerX(ullon, tileWidth(lrlon, ullon, depth), params.get("lrlon"));
//        System.out.println("lowerx" + lowerX);
        int lowerY = findLowerY(ullat, tileHeight(lrlat, ullat, depth), params.get("lrlat"));
//        System.out.println("lowery" + lowerY);
        String[][] tiles = new String[lowerY - upperY + 1][lowerX - upperX];

        tiles = fillArray(tiles, findDepth(params), upperX, upperY, lowerX, lowerY);
        return tiles;
    }

    public double[] rasteredSpecs(Map<String, Double> params) {
        int upperX = findUpperX(ullon, tileWidth(lrlon, ullon, depth), params.get("ullon"));
        int upperY = findUpperY(ullat, tileHeight(lrlat, ullat, depth), params.get("ullat"));
        int lowerX = findLowerX(ullon, tileWidth(lrlon, ullon, depth), params.get("lrlon"));
        int lowerY = findLowerY(ullat, tileHeight(lrlat, ullat, depth), params.get("lrlat"));


        double[] rastered = new double[4];
        double rullon = ullon + tileWidth(lrlon, ullon, depth) * (upperX);
        rastered[0] = rullon;
        double rullat = ullat - tileHeight(lrlat, ullat, depth) * upperY;
        rastered[1] = rullat;
        double rlrlon = ullon + tileWidth(lrlon, ullon, depth) * (lowerX + 1);
        rastered[2] = rlrlon;
        double rlrlat = ullat - tileHeight(lrlat, ullat, depth) * (lowerY + 1);
        //Rlrlat -= tileHeight(lrlat, ullat, findDepth(params));
        rastered[3] = rlrlat;
        return rastered;
    }

    public String[][] fillArray(String[][] tiles, int ddepth, int upperX,
                                 int upperY, int lowerX, int lowerY) {
        tiles = new String[lowerY - upperY + 1][lowerX - upperX + 1];
        for (int i = upperX; i < lowerX + 1; i++) {
            for (int j = upperY; j < lowerY + 1; j++) {
                tiles[j - upperY][i - upperX] = "d" + ddepth + "_x" + i + "_y" + j + ".png";
            }
        }
        return tiles;
    }

    public double tileWidth(double dlrlon, double dullon, int ddepth) {
        double difference = dlrlon - dullon;
        return (difference / (Math.pow(2, ddepth)));
    }

    public double tileHeight(double dlrlat, double dullat, int ddepth) {
        double difference = dullat - dlrlat;
        return (difference / (Math.pow(2, ddepth)));
    }

    public int findUpperX(double start, double dwidth, double end) {
        int x = 0;
        while (start < end) {
            start += dwidth;
            x++;
        }
        if (x > 0) {
            return x - 1;
        } else {
            return 0;
        }
    }

    public int findUpperY(double start, double height, double end) {
        int y = 0;
        while (start > end) {
            start -= height;
            y++;
        }
        if (y > 0) {
            return y - 1;
        } else {
            return 0;
        }
    }

    public int findLowerX(double start, double dwidth, double end) {
        int x = 0;
        while (start < end) {
            start += dwidth;
            x++;
        }
        if (x > 0) {
            return x - 1;
        } else {
            return 0;
        }
    }

    public int findLowerY(double start, double height, double end) {
        int y = 0;
        while (start > end) {
            start -= height;
            y++;
        }
        if (y > 0) {
            return y - 1;
        } else {
            return 0;
        }
    }
}
