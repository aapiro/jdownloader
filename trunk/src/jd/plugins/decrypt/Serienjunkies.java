package jd.plugins.decrypt;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jd.config.ConfigContainer;
import jd.config.ConfigEntry;
import jd.plugins.Plugin;
import jd.plugins.PluginForDecrypt;
import jd.plugins.PluginStep;
import jd.plugins.RequestInfo;
import jd.plugins.event.PluginEvent;
import jd.utils.JDUtilities;

public class Serienjunkies extends PluginForDecrypt {
    static private final String  host = "serienjunkies.safehost.be";
    private String version = "2.0.0.0";
    //http://85.17.177.195/sjsafe/f-e657c0c256dd9e58/rc_h324.html
    private Pattern patternSupported = getSupportPattern("http://85.17.177.195/[+]");
    private Pattern patternCaptcha = Pattern.compile("e/secure/");

    public Serienjunkies() {
        super();
        steps.add(new PluginStep(PluginStep.STEP_DECRYPT, null));
        currentStep = steps.firstElement();
        this.setConfigEelements();
    }
    /*
     * Diese wichtigen Infos sollte man sich unbedingt durchlesen
     */
    @Override public String getCoder() { return "DwD aka James / Botzi";}
    @Override public String getHost() { return host; }
    @Override public String getPluginID() { return "Serienjunkies-2.0.0."; }
    @Override public String getPluginName() { return "SerienJunkies.dl.am"; }
    @Override public Pattern getSupportedLinks() { return patternSupported; }
    @Override public String getVersion() { return version; }
    @Override public boolean isClipboardEnabled() { return true; }
  
   
    
    @Override
    public boolean doBotCheck(File file) {        
        return false;
    }

    @Override
    public PluginStep doStep(PluginStep step, String parameter) {
    	switch (step.getStep()){
    		case PluginStep.STEP_DECRYPT:
    			Vector<String> decryptedLinks = new Vector<String>();
    			try {
    				URL url = new URL(parameter);
    				RequestInfo reqinfo = getRequest(url);
    				Vector<Vector<String>> links;
    				
    				links = getAllSimpleMatches(reqinfo.getHtmlCode(), " <a href=\"http://°\"");
    				Vector<String> helpvector = new Vector<String>();
    				String helpstring = "";
    				
    				//Einzellink
					if(parameter.indexOf("/safe/") >= 0) {
						firePluginEvent(new PluginEvent(this,PluginEvent.PLUGIN_PROGRESS_MAX, 1));
						helpstring = EinzelLinks(parameter);
						firePluginEvent(new PluginEvent(this,PluginEvent.PLUGIN_PROGRESS_INCREASE, null));
						if(check(helpstring))
							decryptedLinks.add(helpstring);
					}
					else if(parameter.indexOf("/sjsafe/") >= 0) {
						firePluginEvent(new PluginEvent(this,PluginEvent.PLUGIN_PROGRESS_MAX, 1));
						helpvector = ContainerLinks(parameter);
						firePluginEvent(new PluginEvent(this,PluginEvent.PLUGIN_PROGRESS_INCREASE, null));
						for(int j=0; j<helpvector.size(); j++) {
							if(check(helpvector.get(j)))
								decryptedLinks.add(helpvector.get(j));
						}
					}
					else {
						firePluginEvent(new PluginEvent(this,PluginEvent.PLUGIN_PROGRESS_MAX, links.size()));
	    				//Kategorien
	    				for(int i=0; i<links.size(); i++) {
	    					firePluginEvent(new PluginEvent(this,PluginEvent.PLUGIN_PROGRESS_INCREASE, null));
	    					if(links.get(i).get(0).indexOf("/safe/") >= 0) {
								helpstring = EinzelLinks(links.get(i).get(0));
								if(check(helpstring))
									decryptedLinks.add(helpstring);
	    					}
	    					else if(links.get(i).get(0).indexOf("/sjsafe/") >= 0) {
	    						helpvector = ContainerLinks(links.get(i).get(0));
	    						for(int j=0; j<helpvector.size(); j++) {
	    							if(check(helpvector.get(j)))
	    								decryptedLinks.add(helpvector.get(j));
	    						}
	    					}
	    					else {
	    						decryptedLinks.add(links.get(i).get(0));
	    						if(check(links.get(i).get(0)))
    								decryptedLinks.add(links.get(i).get(0));
	    					}
	    				}
					}
			   } 
			   catch (MalformedURLException e) { e.printStackTrace(); } 
			   catch (IOException e) { e.printStackTrace(); }
			   
			 //Decrypt abschliessen
   			firePluginEvent(new PluginEvent(this,PluginEvent.PLUGIN_PROGRESS_FINISH, null));
   			step.setParameter(decryptedLinks);
    	}
    	return null;
    }
    
    private void setConfigEelements() {
    	ConfigEntry cfg;
    	config.addEntry(cfg = new ConfigEntry(ConfigContainer.TYPE_LABEL, "Hoster Auswahl"));
        config.addEntry(cfg = new ConfigEntry(ConfigContainer.TYPE_SEPERATOR));
        config.addEntry(cfg = new ConfigEntry(ConfigContainer.TYPE_CHECKBOX, getProperties(), "USE_RAPIDSHARE", "Rapidshare.com"));
        cfg.setDefaultValue(true);
        config.addEntry(cfg = new ConfigEntry(ConfigContainer.TYPE_CHECKBOX, getProperties(), "USE_RAPIDSHAREDE", "Rapidshare.de"));
        cfg.setDefaultValue(false);
        config.addEntry(cfg = new ConfigEntry(ConfigContainer.TYPE_CHECKBOX, getProperties(), "USE_NETLOAD", "Netload.in"));
        cfg.setDefaultValue(false);
    }
    
    //Für Links die bei denen die Parts angezeigt werden
    private Vector<String> ContainerLinks(String url) {
    	Vector<String> links = new Vector<String>();
    	if(!url.startsWith("http://"))
    		url = "http://" + url;
    	
    	try {
    		RequestInfo reqinfo = getRequest(new URL(url));

			for (;;) { // for() läuft bis kein Captcha mehr abgefragt
	        Matcher matcher = patternCaptcha.matcher(reqinfo.getHtmlCode());
	
	        	if (matcher.find()) {
					String captchaAdress = "http://85.17.177.195" + getBetween(reqinfo.getHtmlCode(), "TD><IMG SRC=\"", "\" ALT=\"\" BORDER=\"0\"");
					File dest = JDUtilities.getResourceFile("captchas/" + this.getPluginName() + "/captcha_" + (new Date().getTime()) + ".jpg");
					JDUtilities.download(dest, captchaAdress);
	
					String capTxt = Plugin.getCaptchaCode(dest, this);
					reqinfo = postRequest(new URL(url), "s=" + getBetween(reqinfo.getHtmlCode(), "TYPE=\"HIDDEN\" NAME=\"s\" VALUE=\"", "\"") + "&c=" + capTxt + "&action=Download");
	        	}
	            else {
	                break;
	            }
	        }
			Vector<Vector<String>> links1 = getAllSimpleMatches(reqinfo.getHtmlCode(), "FORM ACTION=\"°\"");
			
			for(int i=0; i<links1.size(); i++) {
				reqinfo = getRequest(new URL(links1.get(i).get(0)));
				reqinfo = getRequest(new URL(getBetween(reqinfo.getHtmlCode(), "SRC=\"", "\"")));
				links.add(reqinfo.getLocation());
			}
    	}
    	catch(IOException e) {
			e.printStackTrace();
		}
    	
		return links;
    }
    
    //Für Links die gleich auf den Hoster relocaten
    private String EinzelLinks(String url) {
    	String links = "";
    	if(!url.startsWith("http://"))
    		url = "http://" + url;
    	
    	try {
    		url = url.replaceAll("safe/rc", "safe/frc");
    		RequestInfo reqinfo = getRequest(new URL(url));

			for (;;) { // for() läuft bis kein Captcha mehr abgefragt
	        Matcher matcher = patternCaptcha.matcher(reqinfo.getHtmlCode());
	
	        	if (matcher.find()) {
					String captchaAdress = "http://85.17.177.195" + getBetween(reqinfo.getHtmlCode(), "TD><IMG SRC=\"", "\" ALT=\"\" BORDER=\"0\"");
					File dest = JDUtilities.getResourceFile("captchas/" + this.getPluginName() + "/captcha_" + (new Date().getTime()) + ".jpg");
					JDUtilities.download(dest, captchaAdress);
	
					String capTxt = Plugin.getCaptchaCode(dest, this);
					reqinfo = postRequest(new URL(url), "s=" + getBetween(reqinfo.getHtmlCode(), "TYPE=\"HIDDEN\" NAME=\"s\" VALUE=\"", "\"") + "&c=" + capTxt + "&dl.start=Download");
	        	}
	            else {
	                break;
	            }
	        }
			links = reqinfo.getLocation();
    	}
    	catch(IOException e) {
			e.printStackTrace();
		}
    	
		return links;
    }
    
    /**
     * Überprüft ob der übergebene Link in der Konfig ausgewählt wurde
     * 
     * @param link
     * @return
     */
    private Boolean check(String link) {
    	if((Boolean) this.getProperties().getProperty("USE_NETLOAD",true) && link.indexOf("netload.in")>=0)
    		return true;
    	if((Boolean) this.getProperties().getProperty("USE_RAPIDSHARE",true) && link.indexOf("rapidshare.com")>=0)
    		return true;
    	if((Boolean) this.getProperties().getProperty("USE_RAPIDSHAREDE",true) && link.indexOf("rapidshare.de")>=0)
    		return true;
    	return false;
    }
}
