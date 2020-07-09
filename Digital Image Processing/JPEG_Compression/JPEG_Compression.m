% imagesc(log(abs(imcoefs)));
% colormap(jet);

%Apply the DCT to the entire image and remove the DCT coefficients below
%the diagonal by setting the coefficients to zero.
image = imread('lena512gray.pgm');
imcoefs = dct2(image);

figure,imshow(imcoefs);
title('DCT Matrix Lena');
saveas(gcf,'lab4_1_2_dct_matrix.pdf');


n_image = nnz(imcoefs);
q3 = triu(imcoefs);

%Display the modified DCT matrix
figure,imshow(q3);
title('Diagonal removed DCT Matrix Lena');
saveas(gcf,'lab4_1_3_moded_dct_matrix.pdf');
% imshow(q3);

% ***Compute the compression ratio assuming 8-bit fixed-length coding to be 
% used to represent each coefficient that is kept. 

n_q3 = nnz(q3);
cr_q3 = (n_image/n_q3);

%Reconstruct the image and display the reconstructed image.
q3_image = uint8(idct2(q3));
figure,imshow(q3_image);
title('Reconstructed from the diagonal DCT matrix Lena');
saveas(gcf,'lab4_1_5_reconed_img.pdf');

ssim_q3v = ssim(q3_image, image);
mse_q3v = mse(q3_image, image);

%6 
fun = @(block_struct)...
     triu(dct2(block_struct.data));
q6 = blockproc(image,[8,8],fun);

n_q6 = nnz(q6);
cr_q6 = (n_image/n_q6);

% ***Compute the compression ratio assuming 8-bit codewords are to be 
% used to represent each coefficient that is kept. 
idct_fun = @(block_struct)...
     idct2(block_struct.data);

q6_image = uint8(blockproc(q6, [8,8], idct_fun));
figure,imshow(q6_image);
title('Reconstructed with block by block lena');
saveas(gcf,'lab4_1_8_reconed_img.pdf');

ssim_q6v = ssim(q6_image, image);
mse_q6v = mse(q6_image, image);

%9
load('qtables.mat')

myQ50fun = @(block_struct)...
     (round(dct2(block_struct.data) ./ Q50) .* Q50);
 
q9_50 = blockproc(image, [8,8], myQ50fun);

myQ90fun = @(block_struct)...
     (round(dct2(block_struct.data) ./ Q90) .* Q90);
 
q9_90 = blockproc(image, [8,8], myQ90fun);

q9_50_image = uint8(blockproc(q9_50, [8,8], idct_fun));
figure,imshow(q9_50_image);
title('Reconstructed Q50 Lena');
saveas(gcf,'lab4_1_11_reconed_q50img.pdf');
q9_90_image = uint8(blockproc(q9_90, [8,8], idct_fun));
figure,imshow(q9_90_image);
title('Reconstructed Q90 Lena');
saveas(gcf,'lab4_1_11_reconed_q90img.pdf');

ssim_q9_50v = ssim(q9_50_image, image);
mse_q9_50v = mse(q9_50_image, image);
ssim_q9_90v = ssim(q9_90_image, image);
mse_q9_90v = mse(q9_90_image, image);

n_q9_50 = nnz(q9_50);
n_q9_90 = nnz(q9_90);
cr_q9_50 = (n_image/n_q9_50);
cr_q9_90 = (n_image/n_q9_90);

% ***Compute the compression ratio assuming 8-bit codewords are to be 
% used to represent each coefficient that is kept.