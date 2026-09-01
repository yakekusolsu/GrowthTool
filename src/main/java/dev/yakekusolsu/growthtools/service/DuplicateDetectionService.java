package dev.yakekusolsu.growthtools.service;

/** Pure decision policy separated from Paper inventory observation. */
public final class DuplicateDetectionService {
    public boolean isDuplicate(int sameOwnerItemCount, boolean previousOwnerStillHasTool) {
        if (sameOwnerItemCount < 0) {
            throw new IllegalArgumentException("sameOwnerItemCount must not be negative");
        }
        return sameOwnerItemCount > 1 || previousOwnerStillHasTool;
    }
}
