package vn.kltn.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;
import vn.kltn.common.DocumentFormat;
import vn.kltn.repository.util.FileUtil;

import java.util.Arrays;
import java.util.List;

public class MultipartFileArrayValidator implements ConstraintValidator<ValidFiles, MultipartFile[]> {

    private static final long MAX_SIZE = 10 * 1024 * 1024; // 10MB
    private static final int MAX_FILES = 5;
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.stream(DocumentFormat.values())
            .map(DocumentFormat::getExtension)
            .map(String::toLowerCase)
            .toList();


    @Override
    public boolean isValid(MultipartFile[] files, ConstraintValidatorContext context) {
        if (files == null || files.length == 0) {
            setMessage(context, "Không có file nào được gửi lên");
            return false;
        }

//        if (files.length > MAX_FILES) {
//            setMessage(context, "Chỉ được upload tối đa " + MAX_FILES + " file");
//            return false;
//        }

        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                setMessage(context, "File " + file.getOriginalFilename() + " bị trống");
                return false;
            }

            if (file.getSize() > MAX_SIZE) {
                setMessage(context, "File " + file.getOriginalFilename() + " vượt quá 10MB");
                return false;
            }

            String extension = FileUtil.getFileExtension(file.getOriginalFilename());
            if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
                setMessage(context, "File " + file.getOriginalFilename() + " không đúng định dạng cho phép");
                return false;
            }
        }

        return true;
    }

    private void setMessage(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }
}
