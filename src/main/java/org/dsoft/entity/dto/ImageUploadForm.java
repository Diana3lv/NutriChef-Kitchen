package org.dsoft.entity.dto;

import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

public class ImageUploadForm {
    @RestForm("image")
    public FileUpload image;
}
