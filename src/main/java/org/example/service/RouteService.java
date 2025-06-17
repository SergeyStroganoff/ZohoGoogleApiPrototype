package org.example.service;

import org.example.entity.google.DistanceGoogleMatrix;

import java.io.IOException;
import java.util.Optional;

public interface RouteService {
    Optional<DistanceGoogleMatrix> getRouteEstimate(String departAddress, String destinationAddresses) throws IOException, InterruptedException;
}
