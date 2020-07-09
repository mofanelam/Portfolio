% Read the lena image downloaded from module webpage.
I = imread('lena512gray.pgm');

% Extract the first two and last two bit planes.
B8 = bitget(I,8)*2^7;
B7 = bitget(I,7)*2^6;
B2 = bitget(I,2)*2^1;
B1 = bitget(I,1)*2^0;

% Display the first two and the last two bit planes.

% subplot(2,2,1);
% imshow(B8, []);
% subplot(2,2,2);
% imshow(B7, []);

% figure
% subplot(2,2,1);
% imshow(B2, []);
% subplot(2,2,2);
% imshow(B1, []);

% Read the logo image downloaded from module webpage.
W = imread('warwick512gray.pgm');

% Display the image.
% imshow(W);

% Determine and find out the appropriate t value.
% Min value is 109
% Mean is 215.2318
% Max value is 255
t = mean(mean(W));
b_logo = zeros(512, 512);

% Represent all intensities above t as 1 and 0 otherwise.
for i = 1:512
    for j = 1:512
        if (W(i,j) < t)
            b_logo(i,j) = 0;
        else
            b_logo(i,j) = 1;
        end
    end
end

% Display the binary logo image.
% imshow(b_logo);

% 5.Create a negative image of the binary logo as uint8 variable and display
% the image.
% Since the max is 1.
negativeImage = uint8(1 - b_logo);

% Display the negative image.
% imshow(negativeImage, []);

temp = uint8(zeros(512,512));
% Watermark the Lena image by replacing LSB.
for i = 1:512
    for j = 1:512
        if (negativeImage(i,j) > 0 )
            temp(i,j) = 1;
        else
            temp(i,j) = 0;
        end
    end
end

w_lena = temp + B2 + B3 + B4 + B5 + B6 + B7 + B8;

% Display the watermarked Lena image
% imshow(w_lena);

% 7.Compute and report the SSIM value between the original image and the
% watermarked Lena image.
ssim_v = ssim(w_lena, lena);

% 8. Store the image as JPEG
imwrite(w_lena, 'new_filename.jpg');
% load the new image and extract the watermark from the LSB plane.
new_image = imread('new_filename.jpg');

% Extract the watermark logo from the LSB plane
% Fetch the LSB bitplane of the jpeg new image.
N1 = bitget(new_image,1)*2^0;

% Using ssim, the value is 0.9614 between the recovered logo and the
% original watermark implemented. However, due to the compression,
% information has lost and the watermark can no longer be found after
% compressed.

% Display the recovered logo.
% imshow(N1, []);

% 9. Create a visible watermark for the Lena image using the original logo
% Use the bit substitution technique discussed in class.

temp3 = uint8(zeros(512,512));
w_bit = bitget(W,8)*2^7;
for i = 1:512
    for j = 1:512
        if (w_bit(i,j) > 0 )
            temp3(i,j) = 32;
        else
            temp3(i,j) = 0;
        end
    end
end
bit_lena = B1 + B2 + B3 + B4 + B5 + temp3 + B7 + B8;

% Display the watermarked image.
% imshow(bit_lena);

