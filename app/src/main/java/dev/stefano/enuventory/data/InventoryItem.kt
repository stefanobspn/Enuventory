package dev.stefano.enuventory.data

import dev.stefano.enuventory.domain.model.Asset
import dev.stefano.enuventory.domain.model.AssetStatus

// TODO: File ini bersifat sementara untuk dummy data selama belum ada repository.
// Akan dihapus saat AssetRepository sudah diimplementasikan.
val dummyAssets = listOf(
    Asset(
        id = "HW-0019-A",
        title = "Arduino Micro Controller",
        status = AssetStatus.Available,
        category = "Elektro",
        description = "Mikrokontroler serbaguna untuk prototyping."
    ),
    Asset(
        id = "HW-0020-B",
        title = "Raspberry Pi 4 Model B",
        status = AssetStatus.Reserved,
        category = "IoT",
        description = "Single-board computer dengan RAM 4GB."
    ),
    Asset(
        id = "HW-0021-C",
        title = "Sensor Ultrasonik HC-SR04",
        status = AssetStatus.Maintenance,
        category = "Elektro",
        description = "Sensor jarak ultrasonik dengan range 2-400cm."
    )
)
