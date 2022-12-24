package com.xplor.app


import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.createBitmap
import androidx.core.graphics.get
import androidx.core.graphics.set
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngQuad
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.RasterLayer
import com.mapbox.mapboxsdk.style.sources.ImageSource
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.zaxxer.sparsebits.SparseBitSet


public var mapView: MapView? = null

private const val ID_IMAGE_SOURCE = "image_source-id"
private const val ID_IMAGE_LAYER = "image_layer-id"

class MainActivity : AppCompatActivity(), OnMapReadyCallback {


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
        var bitmap = BitmapFactory.decodeResource(resources, R.drawable.square)

        mapboxMap.setStyle(
            Style.DARK
        ) { style -> // Set the latitude and longitude values for the image's four corners
            /*val Bigquad = LatLngQuad(
                LatLng(28.60, -81.30),
                LatLng(28.70, -81.30),
                LatLng(28.70, -81.20),
                LatLng(28.60, -81.20)
                )*/

            val quad = LatLngQuad(
                LatLng(28.604297, -81.199357),
                LatLng(28.604052, -81.198778),
                LatLng(28.603411, -81.199105),
                LatLng(28.603703, -81.199776)
            )
                // Add an ImageSource to the map
                style.addSource(
                    ImageSource(
                        ID_IMAGE_SOURCE,
                        quad,
                        bitmap
                    )
                )



                // Create a raster layer and use the imageSource's ID as the layer's data. Then add a RasterLayer to the map.
                style.addLayer(
                    RasterLayer(
                        ID_IMAGE_LAYER,
                        ID_IMAGE_SOURCE
                )
            )
        }
    }

    fun bitmapFromArray(pixels2d: Array<IntArray>): Bitmap {
        val width = pixels2d.size
        val height = pixels2d[0].size
        val pixels = IntArray(width * height)
        var pixelsIndex = 0
        for (i in 0 until width) {
            for (j in 0 until height) {
                pixels[pixelsIndex] = pixels2d[i][j]
                pixelsIndex++
            }
        }
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ALPHA_8)
    }
}

