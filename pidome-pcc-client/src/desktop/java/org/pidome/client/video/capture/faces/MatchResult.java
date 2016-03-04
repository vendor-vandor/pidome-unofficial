package org.pidome.client.video.capture.faces;

import java.io.File;


// MatchResult.java
// Sajan Joseph, sajanjoseph@gmail.com
// http://code.google.com/p/javafaces/
// Modified by Andrew Davison, April 2011, ad@fivedots.coe.psu.ac.th


public class MatchResult
{
  private String matchFnm;
  private double matchDist;


  public MatchResult(String fnm, double dist)
  { matchFnm = fnm;
    matchDist = dist;
  }

  public String getMatchFileName()
  { return matchFnm;  }

  public void setMatchFileName(String fnm)
  { matchFnm = fnm; }

  public double getMatchDistance()
  {  return matchDist;  }

  public void setMatchDistance(double dist)
  {  matchDist = dist;  }


  public String getName()
  {
      if(File.separator.equals("/")){
          return matchFnm.substring(matchFnm.lastIndexOf("savedFaces/")+11, matchFnm.lastIndexOf("/"));
      } else {
          return matchFnm.substring(matchFnm.lastIndexOf("savedFaces\\")+11, matchFnm.lastIndexOf("\\"));
      }
  }  // end of getName()


}  // end of MatchResult class
