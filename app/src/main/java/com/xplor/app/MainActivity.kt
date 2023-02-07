package com.xplor.app


import android.content.Context
import android.graphics.Color
import android.graphics.PointF
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import com.mapbox.bindgen.Expected
import com.mapbox.bindgen.Value
import com.mapbox.geojson.*
import com.mapbox.maps.*
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.extension.style.expressions.dsl.generated.zoom
import com.mapbox.maps.extension.style.expressions.generated.Expression
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.exponential
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.linear
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.literal
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.*
import com.mapbox.maps.extension.style.layers.properties.generated.Visibility
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.xplor.MBTilesServer.stop
import com.xplor.MBTilesSource
import com.xplor.MBTilesSourceException
import java.io.File
import java.io.IOException
import java.io.InputStream


private const val ID_IMAGE_SOURCE = "image_source-id"
private const val ID_IMAGE_LAYER = "image_layer-id"

class MainActivity : AppCompatActivity() {
    lateinit var mapView: MapView
    lateinit var mapboxMap: MapboxMap

    val navigationLocationProvider = NavigationLocationProvider()

    private val locationObserver = object : LocationObserver {
        /**
         * Invoked as soon as the [Location] is available.
         */
        override fun onNewRawLocation(rawLocation: Location) {
            // Not implemented in this example. However, if you want you can also
            // use this callback to get location updates, but as the name suggests
            // these are raw location updates which are usually noisy.
        }

        /**
         * Provides the best possible location update, snapped to the route or
         * map-matched to the road if possible.
         */
        override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
            val enhancedLocation = locationMatcherResult.enhancedLocation
            navigationLocationProvider.changePosition(
                enhancedLocation,
                locationMatcherResult.keyPoints,
            )
            // Invoke this method to move the camera to your current location.
            //updateCamera(enhancedLocation)
        }
    }

    //private lateinit var binding: MapViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        val layerList = ArrayList<String>()
        super.onCreate(savedInstanceState)

        mapView = MapView(this)

        ResourceOptionsManager.getDefault(this, getString(R.string.mapbox_access_token))
        // Display Basic Map
        setContentView(mapView)

        mapboxMap = mapView.getMapboxMap()
        val path = MBTilesSource.readAsset(this, "out.mbtiles")
        val sourceId = "fog-mb-source" // used as Mapbox Source ID
        val mbSource = try {
            MBTilesSource(path, sourceId).apply { activate() }
        } catch (e: MBTilesSourceException.CouldNotReadFileException) {
            Log.d("sus", "Error reading MBTiles!")
            return
        }

        mapboxMap.loadStyleUri(Style.SATELLITE_STREETS) { style ->

            val cameraPosition = CameraOptions.Builder()
                .zoom(9.0)
                .center((Point.fromLngLat(-81.379234, 28.538336)))
                .build()
            mapboxMap.setCamera(cameraPosition)

            // Add fog source GeoJSON
            val fogFeatures = readJSONFromAsset("fog.geojson")?.let {
                FeatureCollection.fromJson(
                    it
                )
            }

            val fogSource = geoJsonSource("fog-source") {
                if (fogFeatures != null) {
                    featureCollection(fogFeatures)
                }
            }
            /*style.addSource(fogSource)

            // Add White Fill in Polygons
            val fogLayer = fillLayer("fog-layer", "fog-source") {
                fillColor(Color.parseColor("#FFFFFF"))
                fillOpacity(interpolate
                                { exponential { literal(1)  }
                                    run { zoom() }
                                    stop {
                                    literal(12.5)
                                    literal(1)
                                }
                                stop {
                                    literal(13)
                                    literal(0)
                                }
                       }
                )

                }
            layerList.add(fogLayer.layerId)
            style.addLayer(fogLayer)*/

            // Add Black Outline to Polygons
            val fogLineLayer = lineLayer("fog-line-layer", "fog-source") {
                lineColor(Color.parseColor("#000000"))
            }
            layerList.add(fogLineLayer.layerId)
            style.addLayer(fogLineLayer)

            // Add country outline GeoJSON
            val countryOutline = readJSONFromAsset("countries.geojson")?.let {
                FeatureCollection.fromJson(
                    it
                )
            }

            val countryOutlineSource = geoJsonSource("country-source") {
                if (countryOutline != null) {
                    featureCollection(countryOutline)
                }
            }
            style.addSource(countryOutlineSource)

            val countryOutlineLayer = lineLayer("country-line-layer", "country-source") {
                lineColor(Color.parseColor("#000000"))
            }
            layerList.add(countryOutlineLayer.layerId)
            style.addLayer(countryOutlineLayer)

            // Add Florida outline GeoJSON
            val floridaOutline = readJSONFromAsset("florida.geojson")?.let {
                FeatureCollection.fromJson(
                    it
                )
            }

            val floridaOutlineSource = geoJsonSource("florida-source") {
                if (floridaOutline != null) {
                    featureCollection(floridaOutline)
                }
            }
            style.addSource(floridaOutlineSource)

            val floridaOutlineLayer = lineLayer("florida-line-layer", "florida-source") {
                lineColor(Color.parseColor("#000000"))
            }
            layerList.add(floridaOutlineLayer.layerId)
            style.addLayer(floridaOutlineLayer)

            style.addSource(mbSource.instance)
            // var mbLineLayer = RasterLayer("fog-mb-layer", mbSource.id)
            /*var mbLineLayer = lineLayer("fog-mb-layer", mbSource.id) {
                lineWidth(literal(100))
                lineColor(Color.parseColor("#FF0000"))
            }*/
            var mbLineLayer = LineLayer("fog-mb-layer", mbSource.id)
            style.addLayer(mbLineLayer)
        }

        mapboxMap.addOnMapClickListener { point ->
            var features : Expected<String, List<QueriedFeature>>? = null
            val userClickLocation = PointF(point.latitude().toFloat(), point.longitude().toFloat())
            val pixel = mapboxMap.pixelForCoordinate(point)
            Log.d("sus", String.format("User clicked at: %s", point.toString()))
            Log.d("sus", mapboxMap.cameraState.zoom.toString());
            mapboxMap.queryRenderedFeatures(
                    RenderedQueryGeometry(pixel),
                    RenderedQueryOptions(layerList, Expression.literal(true))
            ) { queriedFeatures -> lookAtFeature(queriedFeatures)}
            true
        }
    }





            /*val feature = readJSONFromAsset("foggrid.geojson")?.let {
                FeatureCollection.fromJson(
                    it
                )
            }
            //=====================================================
            //                  Bottom Layer Fog

            it.addSource(GeoJsonSource("fog-tile-source", feature))

            it.addLayer(
                FillLayer("fog-tile-layer", "fog-tile-source")
                .withProperties(
                    PropertyFactory.fillColor(Color.parseColor("#FFFFFF"))))

            it.addLayer(LineLayer("fog-tile-outline", "fog-tile-source").withProperties(
                PropertyFactory.lineColor(Color.parseColor("#000000")),
                PropertyFactory.lineWidth(1f)))*/


            //=====================================================
            //                      Top Layer Fog

            /*it.addSource(VectorSource("fog-source", "mapbox://xplor-main.69li4je9"))

            val fogLayer = FillLayer("fog-layer", "fog-source")
                .withProperties(
                    PropertyFactory.fillColor(Color.parseColor("#00FF00")))
                    //PropertyFactory.visibility(step(mapboxMap.cameraPosition.zoom.toInt(), literal(VISIBLE), stop(12, NONE)).toString()))

            fogLayer.setProperties(
                PropertyFactory.fillOpacity(
                    interpolate(
                        exponential(1f), zoom(),
                        stop(12.5f, literal(1f)),
                            stop(13f, literal(0f)))
                ))

            fogLayer.sourceLayer = "foggrid"

            it.addLayer(fogLayer)*/






        /*fun decodeGeoJSON(filename: String) {
            var featureCollection: FeatureCollection = FeatureCollection(readJSONFromAsset(filename))
            var data: String?

            val filePath = URL(filename)
            try {
                data = readJSONFromAsset(filename)
                featureCollection = data
            } catch (e: Exception) {
                Log.d("sus", e.toString())
            }
        }*/

    private fun lookAtFeature(features: Expected<String, List<QueriedFeature>>) {
        if (features.value?.isEmpty() == false) {
            features?.value?.get(0)?.feature?.addStringProperty("fill-color", "#FF0000")
        }
        else
            Log.d("sus6", "No features found here...")
    }
    private fun readJSONFromAsset(filename: String): String? {
        var json: String? = null
        try {
            val  inputStream:InputStream = assets.open(filename)
            json = inputStream.bufferedReader().use{it.readText()}
        } catch (ex: Exception) {
            ex.printStackTrace()
            return null
        }
        return json
    }

    @Throws(IOException::class)
    fun getFileFromAssets(context: Context, fileName: String): File =
        File(context.cacheDir, fileName)
            .also {
                if (!it.exists()) {
                    it.outputStream().use { cache ->
                        context.assets.open(fileName).use { inputStream ->
                            inputStream.copyTo(cache)
                        }
                    }
                }
            }
    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }
}






