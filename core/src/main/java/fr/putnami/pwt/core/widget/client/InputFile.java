/**
 * This file is part of pwt.
 *
 * pwt is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * pwt is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with pwt. If not,
 * see <http://www.gnu.org/licenses/>.
 */
package fr.putnami.pwt.core.widget.client;

import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.dom.client.DataTransfer;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.DragLeaveEvent;
import com.google.gwt.event.dom.client.DragLeaveHandler;
import com.google.gwt.event.dom.client.DragOverEvent;
import com.google.gwt.event.dom.client.DragOverHandler;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.event.dom.client.DropHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.user.client.rpc.StatusCodeException;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RootPanel;

import fr.putnami.pwt.core.event.client.HandlerRegistrationCollection;
import fr.putnami.pwt.core.model.client.base.HasDrawable;
import fr.putnami.pwt.core.service.client.CsrfController;
import fr.putnami.pwt.core.theme.client.CssStyle;
import fr.putnami.pwt.core.theme.client.IconFont;
import fr.putnami.pwt.core.widget.client.Button.Type;
import fr.putnami.pwt.core.widget.client.base.AbstractInput;
import fr.putnami.pwt.core.widget.client.base.SimpleStyle;
import fr.putnami.pwt.core.widget.client.constant.WidgetParams;
import fr.putnami.pwt.core.widget.client.event.ButtonEvent;
import fr.putnami.pwt.core.widget.client.util.StyleUtils;
import fr.putnami.pwt.core.widget.client.util.UUID;
import fr.putnami.pwt.core.widget.shared.domain.FileDto;

public class InputFile extends InputGroup<FileDto> implements HasDrawable {

	private static final CssStyle STYLE_ERROR = new SimpleStyle("has-error");

	private static final String MULTIPART_BOUNDARY = "x-x-x-x-x";
	private static final String EOL = "\r\n";

	private static final String URL_UPLOAD = GWT.getHostPageBaseURL() + "file/upload/";
	private static final String URL_STATUS = GWT.getHostPageBaseURL() + "file/status/";
	private static final String URL_DOWNLOAD = GWT.getHostPageBaseURL() + "file/download/";

	private static final CssStyle STYLE_DRAGOVER = new SimpleStyle("file-dragover");
	private static final CssStyle STYLE_MUTTED = new SimpleStyle("text-muted");
	private class UploadForm {

		private final FlowPanel formPanel = new FlowPanel();
		private final FileUpload fileUpload = new FileUpload();

		private final HandlerRegistrationCollection handlerRegistrations = new HandlerRegistrationCollection();

		public UploadForm() {
			this.formPanel.getElement().getStyle().setHeight(0, Unit.PX);
			this.formPanel.getElement().getStyle().setWidth(0, Unit.PX);
			this.formPanel.getElement().getStyle().setOverflow(Overflow.HIDDEN);
			this.formPanel.add(this.fileUpload);

			this.fileUpload.setName("data");

			this.handlerRegistrations.add(this.fileUpload.addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent event) {
					uploadData(fileUpload.getElement());
				}
			}));
		}

		public UploadForm destroy() {
			InputElement.as(this.fileUpload.getElement()).setValue(null);
			this.handlerRegistrations.removeHandler();
			this.formPanel.removeFromParent();
			return null;
		}

		public void openFilePicker() {
			RootPanel.get().add(this.formPanel);
			InputFile.nativeClick(this.fileUpload.getElement());
		}
	}

	private final WidgetParams params = WidgetParams.Util.get();

	private final OutputProgressBar<Integer> progressBar = new OutputProgressBar<Integer>();
	private final OneWidgetPanel progressBarWrapper = new OneWidgetPanel();
	private final Anchor<?> fileNameAnchor = new Anchor<>();
	private final Text placeholderText = new Text();

	private final Button<?> cancelBtn = new Button<>();
	private final Button<?> uploadBtn = new Button<>();

	private String placeholder = null;

	private String fileId;
	private UploadForm uploadForm;
	private Request sendRequest;


	private String urlUpload = URL_UPLOAD;
	private String urlStatus = URL_STATUS;
	private String urlDownload = URL_DOWNLOAD;


	public InputFile() {
		this.endConstruct();
	}

	protected InputFile(InputFile source) {
		super(source, false);
		this.endConstruct();
	}

	private void endConstruct() {

		this.progressBar.setDisplayValue(true);
		this.progressBar.setAnimated(true);
		this.progressBarWrapper.add(this.progressBar);

		StyleUtils.addStyle(this.progressBarWrapper, AbstractInput.STYLE_CONTROL);
		StyleUtils.addStyle(this.fileNameAnchor, AbstractInput.STYLE_CONTROL);
		StyleUtils.addStyle(this.placeholderText, AbstractInput.STYLE_CONTROL);
		StyleUtils.addStyle(this.placeholderText, InputFile.STYLE_MUTTED);

		this.cancelBtn.setType(Type.ICON);
		this.cancelBtn.setIconType(IconFont.ICON_CANCEL);
		this.cancelBtn.addButtonHandler(new ButtonEvent.Handler() {
			@Override
			public void onButtonAction(ButtonEvent event) {
				if (InputFile.this.sendRequest != null) {
					InputFile.this.sendRequest.cancel();
				}
				InputFile.this.edit(null);
			}
		});
		this.uploadBtn.setType(Type.ICON);
		this.uploadBtn.setIconType(IconFont.ICON_UPLOAD);
		this.uploadBtn.addButtonHandler(new ButtonEvent.Handler() {
			@Override
			public void onButtonAction(ButtonEvent event) {
				if (InputFile.this.uploadForm == null) {
					InputFile.this.uploadForm = new UploadForm();
				}
				InputFile.this.uploadForm.openFilePicker();
			}
		});

		this.addDomHandler(new DragLeaveHandler() {
			@Override
			public void onDragLeave(DragLeaveEvent event) {
				StyleUtils.removeStyle(InputFile.this, InputFile.STYLE_DRAGOVER);
			}
		}, DragLeaveEvent.getType());
		this.addDomHandler(new DragOverHandler() {
			@Override
			public void onDragOver(DragOverEvent event) {
				StyleUtils.addStyle(InputFile.this, InputFile.STYLE_DRAGOVER);
			}
		}, DragOverEvent.getType());
		this.addDomHandler(new DropHandler() {
			@Override
			public void onDrop(DropEvent event) {
				event.preventDefault();
				event.stopPropagation();

				DataTransfer data = event.getNativeEvent().getDataTransfer();
				uploadData(data);
			}
		}, DropEvent.getType());

		this.redraw();
	}

	@Override
	public IsWidget cloneWidget() {
		return new InputFile(this);
	}

	@Override
	public void redraw() {
		this.cancelBtn.removeFromParent();
		this.uploadBtn.removeFromParent();
		this.fileNameAnchor.removeFromParent();
		this.placeholderText.removeFromParent();
		this.progressBarWrapper.removeFromParent();
		if (this.fileId != null) {
			this.append(this.progressBarWrapper);
			this.addAddon(this.cancelBtn);
		} else {
			FileDto value = this.getValue();
			if (value != null) {
				NumberFormat nf = NumberFormat.getFormat("#.##");

				long size = value.getContentLength();
				String displaySize = "";
				if (size > 1024 * 1024) {
					displaySize = nf.format(size / (1024 * 1024D)) + " MB";
				} else if (size > 1024) {
					displaySize = nf.format(size / 1024D) + " KB";
				} else {
					displaySize = nf.format(size) + " B";
				}

				this.fileNameAnchor.setLink(urlDownload + value.getToken());
				this.fileNameAnchor.setText(value.getName() + " - (" + displaySize + ")");
				this.placeholderText.setText(null);
				this.append(this.fileNameAnchor);
				this.addAddon(this.cancelBtn);
			} else {
				this.fileNameAnchor.setLink(null);
				this.fileNameAnchor.setText(null);
				this.placeholderText.setText(this.placeholder);
				this.append(this.placeholderText);
			}
			this.addAddon(this.uploadBtn);
		}
	}

	public String getPlaceholder() {
		return this.placeholder;
	}

	public void setPlaceholder(String placeholder) {
		this.placeholder = placeholder;
		this.placeholderText.setText(placeholder);
	}

	@Override
	public void edit(FileDto object) {
		super.edit(object);
		this.redraw();
	}

	private void initProgressBar() {
		this.progressBar.edit(0);
		this.redraw();
		if (this.params.inputFileProgressEnable()) {
			this.progressBar.edit(0);
		} else {
			this.progressBar.setDisplayValue(false);
			this.progressBar.setMax(100);
			this.progressBar.edit(40);
		}
	}

	private void handleProgress(int total, int loaded) {
		this.progressBar.setMax(total);
		this.progressBar.edit(loaded);
	}

	private void handleCompleteJson(String reponseData) {
		if (Strings.isNullOrEmpty(reponseData)) {
			handleError("500", "Unable to post data");
		}

		JSONObject jsObject = JSONParser.parseLenient(reponseData).isObject();

		final FileDto file = new FileDto();
		file.setName(jsObject.get("name").isString().stringValue());
		file.setExtension(jsObject.get("extension").isString().stringValue());
		file.setMime(jsObject.get("mime").isString().stringValue());
		file.setToken(jsObject.get("token").isString().stringValue());
		file.setContentLength((long) jsObject.get("contentLength").isNumber().doubleValue());

		if (this.uploadForm != null) {
			this.uploadForm = this.uploadForm.destroy();
		}
		this.sendRequest = null;
		this.fileId = null;

		this.progressBar.edit(this.progressBar.getMax());

		Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {

			@Override
			public boolean execute() {
				InputFile.this.edit(file);
				return false;
			}
		}, this.params.inputFileProgressHideDelay());
	}

	private void handleError(String statusCode, String encodedResponse) {
		this.fileId = null;
		progressBar.setValue(0);
		StyleUtils.addStyle(this, STYLE_ERROR);
		throw new StatusCodeException(Integer.parseInt(statusCode), encodedResponse);
	}

	private void uploadData(JavaScriptObject object) {
		StyleUtils.removeStyle(this, STYLE_ERROR);
		this.fileId = UUID.uuid();
		this.initProgressBar();
		nativeUploadData(object, this, urlUpload + this.fileId,
			CsrfController.get().getHeader(), CsrfController.get().getToken());
	}

	public String getUrlUpload() {
		return urlUpload;
	}

	public void setUrlUpload(String urlUpload) {
		this.urlUpload = urlUpload;
	}

	public String getUrlStatus() {
		return urlStatus;
	}

	public void setUrlStatus(String urlStatus) {
		this.urlStatus = urlStatus;
	}

	public String getUrlDownload() {
		return urlDownload;
	}

	public void setUrlDownload(String urlDownload) {
		this.urlDownload = urlDownload;
	}

	private static native void nativeUploadData(JavaScriptObject file, InputFile inputFile, String url,
		String csrfHeader, String csrfToken)
	/*-{
			if(file.files.length === 0){
				return;
			}

	  	var data = new FormData();
			data.append("data", file.files[0]);

			var xhr = new XMLHttpRequest();
			xhr.open("POST", url, true);
			if(csrfToken != null){
						xhr.setRequestHeader(csrfHeader, csrfToken);
			}

			xhr.onreadystatechange=function(){
			  if (xhr.readyState==4 && xhr.status==200) {
						inputFile.@fr.putnami.pwt.core.widget.client.InputFile::handleCompleteJson(Ljava/lang/String;)(xhr.responseText);
			  } else if (xhr.readyState==4){
						inputFile.@fr.putnami.pwt.core.widget.client.InputFile::handleError(Ljava/lang/String;Ljava/lang/String;)(xhr.status, xhr.responseText);
				}
		  }
			xhr.upload.addEventListener('progress', function(e){
						inputFile.@fr.putnami.pwt.core.widget.client.InputFile::handleProgress(II)(e.total, e.loaded);
				}, false);

			xhr.send(data);
	}-*/;

	private static native void nativeClick(Element elem)
	/*-{
		elem.click();
	}-*/;

}
