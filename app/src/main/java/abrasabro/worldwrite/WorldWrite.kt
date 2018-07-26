package abrasabro.worldwrite

import android.app.Application
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.security.ProviderInstaller


class WorldWrite: Application(), ProviderInstaller.ProviderInstallListener {

    override fun onCreate() {
        super.onCreate()
        ProviderInstaller.installIfNeededAsync(this, this)
    }

    /**
     * This method is only called if the provider is successfully updated
     * (or is already up-to-date).
     */
    override fun onProviderInstalled() {
        // Provider is up-to-date, app can make secure network calls.
    }

    /**
     * This method is called if updating fails; the error code indicates
     * whether the error is recoverable.
     */
    override fun onProviderInstallFailed(errorCode: Int, recoveryIntent: Intent) {
        Toast.makeText(this, "Google Play services may not be secure. error $errorCode: ${ConnectionResult(errorCode).errorMessage}", Toast.LENGTH_LONG).show()
    }


}