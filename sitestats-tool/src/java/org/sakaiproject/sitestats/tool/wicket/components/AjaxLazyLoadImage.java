package org.sakaiproject.sitestats.tool.wicket.components;

import java.awt.image.BufferedImage;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Resource;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.image.resource.DynamicImageResource;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.util.time.Duration;
import org.sakaiproject.sitestats.tool.wicket.pages.MaximizedImagePage;

@SuppressWarnings("serial")
public abstract class AjaxLazyLoadImage extends Panel {
	private Link						link								= null;
	private WebMarkupContainer			js									= null;
	private Class						returnPage							= null;
	private boolean						startAjaxUpdate						= false;
	private boolean						doneAjaxUpdate						= false;

	public AjaxLazyLoadImage(final String id, final BufferedImage bufferedImage, final Class returnPage) {
		super(id);
		setOutputMarkupId(true);
		this.returnPage = returnPage;
		final Component loadingComponent = getLoadingComponent("content");
		
		link = createMaximizedLink("link");
		link.setEnabled(false);
		link.add(loadingComponent.setRenderBodyOnly(true));
		add(link);
		
		js = new WebMarkupContainer("js") {
			@Override
			protected void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
				String jsFadein = "jQuery('#"+link.getMarkupId(true)+"').fadeIn();";
				replaceComponentTagBody(markupStream, openTag, jsFadein);
			}	
		};
		js.setOutputMarkupId(true);
		add(js);

		add(new MyAbstractDefaultAjaxBehavior(Duration.ONE_SECOND));
	}
	
	public void startAjaxUpdate() {
		startAjaxUpdate = true;
	}
	
	public void renderImage() {
		link.setEnabled(true);
		link.removeAll();
		link.add(createImage("content", getBufferedImage()));
	}

	/**
	 * @param markupId
	 *            The components markupid.
	 * @return The component to show while the real component is being created.
	 */
	public Component getLoadingComponent(String markupId) {
		Label indicator = new Label(markupId, "<img src=\"" + RequestCycle.get().urlFor(AbstractDefaultAjaxBehavior.INDICATOR) + "\"/>");
		indicator.setEscapeModelStrings(false);
		indicator.add(new AttributeModifier("title", true, new Model("...")));
		return indicator;
	}

	public abstract BufferedImage getBufferedImage();

	public abstract BufferedImage getBufferedMaximizedImage();

	private Link createMaximizedLink(final String id) {
		Link link = new Link(id) {
			@Override
			public void onClick() {
				setResponsePage(new MaximizedImagePage(getBufferedMaximizedImage(), returnPage));
			}			
		};
		link.setOutputMarkupId(true);
		return link;
	}
	
	private Image createImage(final String id, final BufferedImage bufferedImage) {
		NonCachingImage chartImage = new NonCachingImage(id) {
			@Override
			protected Resource getImageResource() {
				return new DynamicImageResource() {

					@Override
					protected byte[] getImageData() {
						return toImageData(bufferedImage);
					}

					@Override
					protected void setHeaders(WebResponse response) {
						response.setHeader("Pragma", "no-cache");
						response.setHeader("Cache-Control", "no-cache");
						response.setDateHeader("Expires", 0);
						response.setContentType("image/png");
						response.setContentLength(getImageData().length);
						response.setAjax(true);
					}
				}.setCacheable(false);
			}
		};
		chartImage.setOutputMarkupId(true);
		return chartImage;
	}
	
	class MyAbstractDefaultAjaxBehavior extends AbstractAjaxTimerBehavior {
		
		public MyAbstractDefaultAjaxBehavior(Duration updateInterval) {
			super(updateInterval);
		}

		@Override
		protected void onTimer(AjaxRequestTarget target) {
			if(startAjaxUpdate) {
				if(!doneAjaxUpdate) {
					renderImage();
					target.addComponent(link);
					target.addComponent(js);
					doneAjaxUpdate = true;
					stop();
				}
			}
		}

		public void renderHead(IHeaderResponse response) {
			super.renderHead(response);
			response.renderOnDomReadyJavascript(getCallbackScript(false).toString());
		}
		
	}
}
