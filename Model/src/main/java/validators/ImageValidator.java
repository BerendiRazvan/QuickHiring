package validators;

import domain.ImageData;

public class ImageValidator implements Validator<ImageData> {
    @Override
    public void validate(ImageData imageData) throws ValidatorException {
        if (imageData.getHeight() == 0 || imageData.getWidth() == 0 ||
                imageData.getHeight() > 3000 || imageData.getWidth() > 3000 ||
                imageData.getImageData().length == 0 ||
                imageData.getImageData().length >= imageData.getHeight() * imageData.getWidth() * 4 + 1)
            throw new ValidatorException("Invalid image!");
    }
}
