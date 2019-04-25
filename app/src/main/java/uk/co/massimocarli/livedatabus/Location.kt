package uk.co.massimocarli.livedatabus

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData

/**
 * This is the sealed type which contains all the possible events that a LiveData can
 * generate
 */
sealed class LocationEvent

class LocationData(val location: Location?) : LocationEvent()
object PermissionRequest : LocationEvent()

class LocationLiveData(val context: Context, val locationManager: LocationManager) : LiveData<LocationEvent>(),
    LocationListener by emptyLocationListener {

    companion object {
        lateinit var instance: LocationLiveData
        const val LOCATION_PROVIDER = LocationManager.NETWORK_PROVIDER
        operator fun invoke(locationManager: LocationManager): LocationLiveData {
            if (!::instance.isInitialized) {
                instance = LocationLiveData(locationManager)
            }
            return instance
        }
    }

    override fun onActive() {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // We get a first location information
            val lastKnownLocation: Location? = locationManager.getLastKnownLocation(LOCATION_PROVIDER)
            postValue(LocationData(lastKnownLocation))
            // We start to listen to the location provider
            locationManager.requestLocationUpdates(LOCATION_PROVIDER, 0L, 0f, this)
        } else {
            postValue(PermissionRequest)
        }
    }

    override fun onInactive() {
        locationManager.removeUpdates(this)
    }

    override fun onLocationChanged(location: Location?) {
        postValue(LocationData(location))
    }

    fun permissionUpdate() {
        onActive()
    }
}

val emptyLocationListener = object : LocationListener {
    override fun onLocationChanged(location: Location?) {
        // Do nothing
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        // Do nothing
    }

    override fun onProviderEnabled(provider: String?) {
        // Do nothing
    }

    override fun onProviderDisabled(provider: String?) {
        // Do nothing
    }
}