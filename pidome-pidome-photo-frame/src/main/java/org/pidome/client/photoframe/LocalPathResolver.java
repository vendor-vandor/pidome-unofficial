/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.photoframe;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author John
 */
public class LocalPathResolver {
    
    protected static Path getLocalBasePath() throws RuntimeException {
        try {
            return Paths.get(LocalPathResolver.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
        } catch (URISyntaxException ex) {
            throw new RuntimeException ("Unable to resolve application path");
        }
    }
    
}
