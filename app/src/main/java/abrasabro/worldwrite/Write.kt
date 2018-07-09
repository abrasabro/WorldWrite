package abrasabro.worldwrite

import android.databinding.BaseObservable
import android.databinding.Bindable


class Write: BaseObservable(){

    var message: String = "Default Message"
        @Bindable get() {return field}
        set(value) {field = value
            notifyPropertyChanged(BR.message)}
    var lat: Double = 0.0
        @Bindable get() {return field}
        set(value) {field = value
            notifyPropertyChanged(BR.lat)}
    var lon: Double = 0.0
        @Bindable get() {return field}
        set(value) {field = value
            notifyPropertyChanged(BR.lon)}
    var address: String = "near Default Address"
        @Bindable get() {return field}
        set(value) {field = value
            notifyPropertyChanged(BR.address)}
    var ratingGood: Int = 0
        @Bindable get() {return field}
        set(value) {field = value
            notifyPropertyChanged(BR.ratingGood)}
    var ratingPoor: Int = 0
        @Bindable get() {return field}
        set(value) {field = value
            notifyPropertyChanged(BR.ratingPoor)}
    var messageUID: String = ""
        @Bindable get() {return field}
        set(value) {field = value
            notifyPropertyChanged(BR.messageUID)}

    fun set(write: Write){
        message = write.message
        lat = write.lat
        lon = write.lon
        address = write.address
        ratingGood = write.ratingGood
        ratingPoor = write.ratingPoor
        messageUID = write.messageUID
    }
    fun set(message: String, lat: Double, lon: Double, address: String, ratingGood: Int = 0, ratingPoor: Int = 0, messageUID: String = ""){

    }
}