package com.lazrproductions.cuffed.cap.base;

import com.lazrproductions.cuffed.cap.RestrainableCapability;

/** Implemented on {@code Player} by PlayerMixin; replaces the Forge capability attachment. */
public interface IRestrainableHolder {
    RestrainableCapability cuffed$getRestrainable();

    void cuffed$setRestrainable(RestrainableCapability value);
}
