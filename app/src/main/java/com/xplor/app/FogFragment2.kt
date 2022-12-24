package com.xplor.app

import android.os.Bundle
import android.os.Debug
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngQuad
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.layers.RasterLayer
import com.mapbox.mapboxsdk.style.sources.ImageSource


private const val ID_IMAGE_SOURCE = "image_source-id"
private const val ID_IMAGE_LAYER = "image_layer-id"

/**
 * A simple [Fragment] subclass.
 * Use the [FogFragment2.newInstance] factory method to
 * create an instance of this fragment.
 */
class FogFragment2 : Fragment(R.layout.fragment_fog2) {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        //arguments.getSerializable("test", Style)?.let { Log.d("sus", it) }
        //AddSquareToMap(arguments!!.getParcelable<Style>("test")!!)
        return inflater.inflate(R.layout.fragment_fog2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);


        // Add squares to map
        //this.activity?.let { AddSquareToMap(this.activity.) };
    }

    fun AddSquareToMap(mapStyle: Style) {
        Log.d("sus", "ADDING SQUASRE")
        //mapStyle.addImage("square", BitmapFactory.decodeResource(this.resources, R.drawable.square))

        //mapStyle.addSource(GeoJsonSource("source-id"))
        val quad = LatLngQuad(
            LatLng(28.538336, -81.379234),
            LatLng(28.538336, -81.378234),
            LatLng(28.539336, -81.379234),
            LatLng(28.539336, -81.378234)
        )

// Add the image to the map style.
        //mapStyle.addImage('cat', image);

// Add a data source containing one point feature.
        //mapStyle.addSource(ImageSource(ID_IMAGE_SOURCE, quad, R.drawable.square))

        // Add layer
        val layer = RasterLayer(ID_IMAGE_LAYER, ID_IMAGE_SOURCE)
        mapStyle.addLayer(layer)
    }
}