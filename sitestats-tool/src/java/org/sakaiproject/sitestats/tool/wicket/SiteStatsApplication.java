package org.sakaiproject.sitestats.tool.wicket;

import java.util.Locale;

import org.apache.wicket.Component;
import org.apache.wicket.protocol.http.HttpSessionStore;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.resource.loader.IStringResourceLoader;
import org.apache.wicket.session.ISessionStore;
import org.apache.wicket.settings.IExceptionSettings;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.locator.ResourceStreamLocator;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.sitestats.tool.facade.SakaiFacade;
import org.sakaiproject.sitestats.tool.wicket.pages.OverviewPage;
import org.sakaiproject.util.ResourceLoader;


public class SiteStatsApplication extends WebApplication {
	
	private SakaiFacade	facade;

	protected void init() {
		super.init();

		// Configure general wicket application settings
		addComponentInstantiationListener(new SpringComponentInjector(this));
		getResourceSettings().setThrowExceptionOnMissingResource(true);
		getMarkupSettings().setStripWicketTags(true);
		getDebugSettings().setAjaxDebugModeEnabled(ServerConfigurationService.getBoolean("sitestats.ajaxDebugEnabled", false));
		getResourceSettings().addStringResourceLoader(new SiteStatsStringResourceLoader());
		getResourceSettings().addResourceFolder("html");
		getResourceSettings().setResourceStreamLocator(new SiteStatsResourceStreamLocator());

		// Home page
		mountBookmarkablePage("/home", OverviewPage.class);

		// On wicket session timeout or wicket exception, redirect to main page
		getApplicationSettings().setPageExpiredErrorPage(OverviewPage.class);
		getApplicationSettings().setInternalErrorPage(OverviewPage.class);

		// show internal error page rather than default developer page
		getExceptionSettings().setUnexpectedExceptionDisplay(IExceptionSettings.SHOW_INTERNAL_ERROR_PAGE);
	}

	@SuppressWarnings("unchecked")
	public Class getHomePage() {
		return OverviewPage.class;
	}

	public SakaiFacade getFacade() {
		return facade;
	}

	public void setFacade(final SakaiFacade facade) {
		this.facade = facade;
	}

	@Override
	protected ISessionStore newSessionStore() {
		// SecondLevelCacheSessionStore causes problems with Ajax requests;
		// => use HttpSessionStore instead.
		return new HttpSessionStore(this);
	}

	/**
	 * Custom bundle loader to pickup bundles from sitestats-bundles/
	 * @author Nuno Fernandes
	 */
	private static class SiteStatsStringResourceLoader implements IStringResourceLoader {
		private ResourceLoader	messages	= new ResourceLoader("Messages");
		private ResourceLoader	events		= new ResourceLoader("Events");

		public String loadStringResource(Component component, String key) {
			String value = messages.getString(key);
			if(value == null){
				value = events.getString(key);
			}
			if(value == null){
				value = key;
			}
			return value;
		}

		public String loadStringResource(Class clazz, String key, Locale locale, String style) {
			ResourceLoader msgs = new ResourceLoader("Events");
			msgs.setContextLocale(locale);
			String value = msgs.getString(key);
			if(value == null){
				ResourceLoader evnts = new ResourceLoader("Events");
				evnts.setContextLocale(locale);
				value = evnts.getString(key);
			}
			if(value == null){
				value = key;
			}
			return value;
		}

	}
	
	/**
	 * Custom loader for .html files
	 * @author Nuno Fernandes
	 */
	private static class SiteStatsResourceStreamLocator extends ResourceStreamLocator {

		public SiteStatsResourceStreamLocator() {
		}

		public IResourceStream locate(final Class clazz, final String path) {
			IResourceStream located = super.locate(clazz, trimFolders(path));
			if(located != null){
				return located;
			}
			return super.locate(clazz, path);
		}

		private String trimFolders(String path) {
			String wicketPackage = "/wicket/";
			return path.substring(path.lastIndexOf(wicketPackage) + wicketPackage.length());
		}
	}
}
