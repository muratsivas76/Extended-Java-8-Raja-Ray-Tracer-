/* $Id: AdvancedRayTracer.java,v 1.2 2001/02/25 01:25:18 gregoire Exp $
 * Copyright (C) 1999-2001 E. Fleury & G. Sutre
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package raja.renderer;

import raja.*;
import raja.light.LightSource;
import raja.shape.*;

import java.awt.image.BufferedImage;
import java.util.Iterator;


public class AdvancedRayTracer implements RayTracer
{
  private World world;
  private RGB cutLevel;
  private int maxDepth;
  
  public AdvancedRayTracer(World world, int maxDepth, float accuracy)
  {
    this.world = world;
    this.maxDepth = maxDepth;
    computeCutLevel(accuracy);
  }
  public AdvancedRayTracer(World world, int maxDepth)
  {
    this(world, maxDepth, 1);
  }
  
  private void computeCutLevel(float accuracy)
  {
    RGB maxLight = RGB.max(world.getAmbiantLight(), world.getBackgroundLight());
    
    for(Iterator iterLight = world.lightIterator() ; iterLight.hasNext() ;)
    {
      LightSource currentLightSource = (LightSource) iterLight.next();
      maxLight = RGB.sum(maxLight, currentLightSource.getMax());
    }
    cutLevel = RGB.product(RGB.product(RGB.inverse(maxLight), 1.0 / (3 * 255)), accuracy);
  }
  
  public RGB getLight(Ray ray)
  {
    return acceleratedRecursiveRay(maxDepth, ray, RGB.white);
  }
  private RGB acceleratedRecursiveRay(int depth, Ray ray, RGB coeff)
  {
    // Cherche le point d'intersection du rayon ray avec les objets et :
    // 1. s'il n'y a pas d'intersection : on renvoie la lumière de fond
    // 2. sinon, on renvoie la somme de localLight, et de reflectedLight
    // au point d'intersection (et de refractedLight... a implementer)
    
    if ((depth <= 0) || coeff.isSmaller(cutLevel))
    {
      return RGB.black;
    }
    
    SolidLocalGeometry intersection = world.getSolid().intersection(ray);
    
    if (intersection == null)
    {
      // Le rayon n'a rencontré aucun objet 3D du monde
      // On colorie donc le pixel avec la couleur de fond
      
      return RGB.product(world.getBackgroundLight(), coeff);
    }
    else
    {
      // Le rayon a rencontré un objet. Le premier objet qu'il
      // rencontré est donné par la variable "solid" et l'intersection
      // se fait au point donné par la variable "intersection"
      
      Vector3D normalAtIntersection = intersection.getNormal();
      double sc = Vector3D.dotProduct(normalAtIntersection, ray.direction);
      Vector3D mirrorDirection = Vector3D.sum(ray.direction, Vector3D.product(normalAtIntersection, - 2 * sc));
      
      boolean rayIntersectsIn = (sc > 0);
      
      LocalTexture localTexture;
      if (rayIntersectsIn) {
        localTexture = intersection.getInLocalTexture();
      }
      else {
        localTexture = intersection.getOutLocalTexture();
      }
      
      RGB krg = localTexture.getKrg();
      RGB ktg = localTexture.getKtg();
      
      RGB result = RGB.product(localLight(ray, intersection, normalAtIntersection, mirrorDirection, localTexture, rayIntersectsIn), coeff);
      
      if (! (RGB.product(ktg, coeff)).isSmaller(cutLevel)) {
        double nI;
        double nT;
        
        if (rayIntersectsIn) {
          if (intersection.inVolumeUndefined()) {
            nI = world.getAmbiantRefractiveIndex(intersection);
          }
          else {
            nI = intersection.getInRefractiveIndex();
          }
          
          if (intersection.outVolumeUndefined()) {
            nT = world.getAmbiantRefractiveIndex(intersection);
          }
          else {
            nT = intersection.getOutRefractiveIndex();
          }
        }
        else {
          if (intersection.inVolumeUndefined()) {
            nT = world.getAmbiantRefractiveIndex(intersection);
          }
          else {
            nT = intersection.getInRefractiveIndex();
          }
          
          if (intersection.outVolumeUndefined()) {
            nI = world.getAmbiantRefractiveIndex(intersection);
          }
          else {
            nI = intersection.getOutRefractiveIndex();
          }
        }
        
        double k = nI / nT;
        
        /*
        double thetaI = Math.acos(Math.abs(sc));
        double thetaT = Math.asin(k * Math.sin(thetaI));
        
        if (Double.isNaN(thetaT)) {
          // Réflexion totale : pas de rayon réfracté
        krg = RGB.sum(krg, ktg);
        }
        else {
          double reflex = 0.5 * (Math.pow(Math.sin(thetaI - thetaT), 2) / Math.pow(Math.sin(thetaI + thetaT), 2))
         * (1 + (Math.pow(Math.cos(thetaI + thetaT), 2) / Math.pow(Math.cos(thetaI - thetaT), 2)));
        
        krg = RGB.sum(krg, RGB.product(ktg, reflex));
        ktg = RGB.product(ktg, 1 - reflex);
        
        double square = 1 - (k*k * (1 - sc*sc));
        double sc2;
        
        if (rayIntersectsIn) {
          sc2 = Math.sqrt(square);
        }
        else {
          sc2 = - Math.sqrt(square);
        }
         */
        
        double cosThetaI = Math.abs(sc);
        double sinThetaI = Math.sqrt(1 - cosThetaI*cosThetaI);
        double sinThetaT = k * sinThetaI;
        
        if (sinThetaT > 1) {
          // Réflexion totale : pas de rayon réfracté
          krg = RGB.sum(krg, ktg);
        }
        else {
          double cosThetaT = Math.sqrt(1 - sinThetaT*sinThetaT);
          
          double tmp1, tmp2;
          
          tmp1 = sinThetaT*cosThetaI;
          tmp2 = cosThetaT*sinThetaI;
          double sinThetaIplusThetaT  = tmp1 + tmp2;
          double sinThetaImoinsThetaT = tmp1 - tmp2;
          
          tmp1 = cosThetaI*cosThetaT;
          tmp2 = sinThetaI*sinThetaT;
          double cosThetaIplusThetaT  = tmp1 - tmp2;
          double cosThetaImoinsThetaT = tmp1 + tmp2;
          
          double reflex = 0.5 * ((sinThetaImoinsThetaT*sinThetaImoinsThetaT) / (sinThetaIplusThetaT*sinThetaIplusThetaT))
          * (1 + ((cosThetaIplusThetaT*cosThetaIplusThetaT) / (cosThetaImoinsThetaT*cosThetaImoinsThetaT)));
          
          krg = RGB.sum(krg, RGB.product(ktg, reflex));
          ktg = RGB.product(ktg, 1 - reflex);
          
          double sc2;
          
          if (rayIntersectsIn) {
            sc2 = cosThetaT;
          }
          else {
            sc2 = - cosThetaT;
          }
          
          Vector3D transmissionDirection = Vector3D.sum(Vector3D.product(ray.direction, k),
          Vector3D.product(normalAtIntersection, sc2 - (k * sc)));
          Ray refractedRay = new Ray(intersection, transmissionDirection);
          result = RGB.sum(result, acceleratedRecursiveRay(depth - 1, refractedRay, RGB.product(ktg, coeff)));
        }
      }
      Ray reflectedRay = new Ray(intersection, mirrorDirection);
      result = RGB.sum(result, acceleratedRecursiveRay(depth - 1, reflectedRay, RGB.product(krg, coeff)));
      
      return result;
    }
  }
  private RGB localLight(Ray ray, Point3D intersection, Vector3D normalAtIntersection, Vector3D mirrorDirection, LocalTexture localTexture, boolean rayIntersectsIn)
  {
    // On détermine la lumière reçue par le point d'intersection
    
    RGB outgoingLightAtIntersection = RGB.product(world.getAmbiantLight(), localTexture.getKd());
    
    for(Iterator iterLight = world.lightIterator() ; iterLight.hasNext() ;)
    {
      LightSource currentLightSource = (LightSource) iterLight.next();
      LightRay rayToLightSource = currentLightSource.getLightRay(intersection);
      
      if (rayToLightSource == null) {
        continue;
      }
      
      double dotProduct;
      
      if (rayIntersectsIn) {
        dotProduct = - Vector3D.dotProduct(normalAtIntersection, rayToLightSource.direction);
      }
      else {
        dotProduct = Vector3D.dotProduct(normalAtIntersection, rayToLightSource.direction);
      }
      
      boolean isInShadow = false;
      
      // On détermine si le point d'intersection est dans l'ombre
      // de la lumière en cours de traitement
      
      if (dotProduct < 0) {
        isInShadow = true;
      }
      else if (world.getSolid().intersects(rayToLightSource)) {
        isInShadow = true;
      }
      
      if (! isInShadow)
      {
        // Le point d'intersection est illuminé par la source de lumière en cours de traitement
        // et on ajoute donc la contribution de cette source de lumière
        
        RGB diffusionLight = RGB.product(RGB.product(rayToLightSource.light, dotProduct), localTexture.getKd());
        double scHalo = Vector3D.dotProduct(rayToLightSource.direction, mirrorDirection);
        RGB reflectionLight = RGB.product(rayToLightSource.light, Math.pow(Math.max(0, scHalo), localTexture.getNs()));
        RGB localLight = RGB.sum(diffusionLight, RGB.product(reflectionLight, localTexture.getKrl()));
        outgoingLightAtIntersection = RGB.sum(outgoingLightAtIntersection, localLight);
      }
    }
    return outgoingLightAtIntersection;
  }
}
