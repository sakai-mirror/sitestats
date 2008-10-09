package org.sakaiproject.sitestats.tool.wicket.components;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.extensions.ajax.markup.html.WicketAjaxIndicatorAppender;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.model.IModel;


public abstract class IndicatingAjaxRadioGroup extends RadioGroup implements IAjaxIndicatorAware {
	private static final long						serialVersionUID	= 1L;
	private AjaxFormChoiceComponentUpdatingBehavior	ajaxUpdatingBehavior;
	private Object 									forModelObjectOnly;
	private WicketAjaxIndicatorAppender				indicatorAppender;
	private WicketAjaxIndicatorAppender				customAjaxIndicator;

	public IndicatingAjaxRadioGroup(final String id) {
		this(id, null, null);
	}

	public IndicatingAjaxRadioGroup(final String id, final Object forModelObjectOnly) {
		this(id, null, forModelObjectOnly, null);
	}

	public IndicatingAjaxRadioGroup(final String id, final Object forModelObjectOnly, final WicketAjaxIndicatorAppender customAjaxIndicator) {
		this(id, null, forModelObjectOnly, customAjaxIndicator);
	}
	
	public IndicatingAjaxRadioGroup(final String id, final IModel model, final Object forModelObjectOnly, final WicketAjaxIndicatorAppender customAjaxIndicator) {
		super(id, model);
		setOutputMarkupId(true);
		this.forModelObjectOnly = forModelObjectOnly;
		if(customAjaxIndicator != null) {
			this.customAjaxIndicator = customAjaxIndicator;
		}else{
			indicatorAppender = new WicketAjaxIndicatorAppender();
			add(indicatorAppender);
		}
		ajaxUpdatingBehavior = new AjaxFormChoiceComponentUpdatingBehavior() {
			private static final long	serialVersionUID	= 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				if(forModelObjectOnly != null && forModelObjectOnly.equals(getModelObject())) {
					IndicatingAjaxRadioGroup.this.onUpdate(target);
				}
			}
		};
		add(ajaxUpdatingBehavior);
	}

	public void removeAjaxUpdatingBehavior() {
		if(getBehaviors().contains(ajaxUpdatingBehavior)) {
			remove(ajaxUpdatingBehavior);
		}
	}

	/**
	 * Listener method invoked on an ajax update call
	 * @param target
	 */
	protected abstract void onUpdate(AjaxRequestTarget target);

	public String getAjaxIndicatorMarkupId() {
		if(forModelObjectOnly != null && forModelObjectOnly.equals(getModelObject())) {
			if(customAjaxIndicator != null) {
				return customAjaxIndicator.getMarkupId();
			}else{
				return indicatorAppender.getMarkupId();
			}
		}else{
			return null;
		}
	}

}
