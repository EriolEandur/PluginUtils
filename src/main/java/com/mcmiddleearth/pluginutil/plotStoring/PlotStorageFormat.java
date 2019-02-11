/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mcmiddleearth.pluginutil.plotStoring;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 *
 * @author Eriol_Eandur
 */
public interface PlotStorageFormat {
    
    public void save(IStoragePlot plot, DataOutputStream out) throws IOException;
    
    public void load(IStoragePlot plot, DataInputStream in) throws IOException, InvalidRestoreDataException;

    public void load(Location location, Vector size, DataInputStream in) throws IOException, InvalidRestoreDataException;
    
}
