package com.xplor.app


import android.graphics.Color
import android.graphics.PointF
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.JsonElement
import com.mapbox.geojson.*
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.FillLayer
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.style.sources.VectorSource.*
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import java.io.InputStream


private const val ID_IMAGE_SOURCE = "image_source-id"
private const val ID_IMAGE_LAYER = "image_layer-id"

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    public var mapView: MapView? = null
    public var mapboxMap: MapboxMap? = null

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
        super.onCreate(savedInstanceState)

        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        // Display Basic Map


        // Lambda!
        /*mapView?.getMapboxMap()?.loadStyleUri(Style.MAPBOX_STREETS) {

            // Activate location tracking
            mapView!!.location.updateSettings {
                enabled = true;
                pulsingEnabled = true;
                // Activate bearing tracking
                mapView!!.location2.puckBearingSource = PuckBearingSource.HEADING;
                mapView!!.location2.puckBearingSource = PuckBearingSource.COURSE;

                mapView!!.location.apply {
                    this.locationPuck = LocationPuck2D(
                        bearingImage = ContextCompat.getDrawable(
                            this@MainActivity,
                                R.drawable.temp_nav_icon
                        )
                    )
                    setLocationProvider(navigationLocationProvider)
                    enabled = true
                }
                *//*val bundle = Bundle()
                bundle.putParcelable("test", it)
                // set Fragmentclass Arguments
                val fragobj = FogFragment2()
                fragobj.arguments = bundle*//*


            }

        }*/

        setContentView(R.layout.activity_main)
        mapView = findViewById(R.id.mapView)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(this)


    }

    @Override
    override fun onMapReady(mapboxMap: MapboxMap) {
        //var bitmap = BitmapFactory.decodeResource(resources, R.drawable.geo_square)
        var geoJson: String
        val routeCoordinates = ArrayList<Point>()
        routeCoordinates.add(Point.fromLngLat(-118.394391, 33.397676))
        routeCoordinates.add(Point.fromLngLat(-118.370917, 33.391142))

        val lineString = LineString.fromLngLats(routeCoordinates)

        val feature2 = Feature.fromGeometry(lineString)
        mapboxMap.setStyle(Style.DARK)

        mapboxMap.getStyle {
            val feature = readJSONFromAsset("foggrid.geojson")?.let {
                FeatureCollection.fromJson(
                    it
                )
            }

            Log.d("sus", feature.toString())
            it.addSource(GeoJsonSource("fog-source", feature))

            it.addLayer(
                FillLayer("fog-layer", "fog-source")
                .withProperties(
                    PropertyFactory.fillColor(Color.parseColor("#FFFFFF"))))

            it.addLayer(LineLayer("fog-outline", "fog-source").withProperties(
                PropertyFactory.lineColor(Color.parseColor("#000000")),
                PropertyFactory.lineWidth(1f)))


        }

        mapboxMap.addOnMapClickListener { point ->
            val userClickLocation = PointF(point.latitude.toFloat(), point.longitude.toFloat())
            val pixel = mapboxMap.projection.toScreenLocation(point)
            Log.d("sus", String.format("User clicked at: %s", point.toString()))
            val features =
                mapboxMap.queryRenderedFeatures(pixel)
            if(features.isNotEmpty()) {
                Log.d("sus", "yeeeeeeeeeee!")
                Log.d("sus", features.toString())
                features.get(0).addStringProperty("fill-color", "#FF0000")
            }
            true
        }
        }

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
}




