% Generate a random watermark vector w of length n = 500. 
% Make w to have zero mean and unit standard deviation.
m = 0;
st_d = 1;
n = 1500;
w = st_d .* randn(1,n) + m;

% Load the Lena image again as before. Apply a 2D DCT to the Lena image. 
% Display the DCT coefficients matrix as an image.
I = imread('lena512gray.pgm');
F = dct2(I);
% Store the origin DCT coefficients matrix.
f = dct2(I);
% imshow(F);


% In the DCT coeff matrix, locate the n largest DCT coefficients 
% (absolute value). Do not include the DC coefficient i.e., the first 
% (and the largest element) in the DCT matrix. Let h = [h1, ..., hn] be 
% the vector containing these values, where h1 is the largest and hn 
% is the smallest DCT coefficient.

% Initialize the vector, h.
% Get only the n largest DCT coefficient
h = zeros(1, n);

% Get the size of the I.
[row, col] = size(I);

% Fetch the n largest DCT coefficient
count = 0;
count2 = 0;
for i = 1:1:row
    for j = 1:1:col 
        if(i == 1 && j == 1)
            continue;
        end
        for k = 1:1:n
           if(abs(F(i,j)) > abs(h(1,k)))
               h(1,k+1:end) = h(1,k:end-1);
               h(1,k) = F(i,j);
               break;
           end
        end
    end
end

% Generate the watermarked coefficients
h_waterc = zeros(1, n);
alpha = 0.1;
for i = 1:1:n
    h_waterc(1,i) = (h(1,i)*(1 + alpha*w(1,i))); 
end

% In the DCT matrix, place the watermarked coefficients in the right locations. 
for i = 1:1:row
    for j = 1:1:col 
        for k = 1:1:n
            if(F(i,j) == h(1,k))
                F(i,j) = h_waterc(1,k);
            end
        end
    end
end

% Display the modified DCT coefficients matrix.
% imshow(F);

% Apply inverse DCT on the watermarked coefficients. 
% Display the image.
G = uint8(idct2(F));
% imshow(G);

% Compute SSIM between this watermarked image and 
% the original unwatermarked image.
ssim_value = ssim(I, G);

% Repeat the experiment with different values of n = {100, 1000, 1500}. 
% Report any observation you may have.
% SSIM Value 1500: 0.9938  1000:0.9948  500:0.9960  100:0.9973

