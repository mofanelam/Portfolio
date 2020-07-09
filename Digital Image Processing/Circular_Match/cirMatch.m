clc;
clear;
% Read in the image and convert it into grayscaled image.
test = imread('lena512gray.pgm');
test = imCshift(test, 50, 100);
figure,imshow(test);
title('Testing on imCshift against Lena image');
saveas(gcf,'lab5_1_2_imCshift_lena.pdf');

I = imread('jeep.png');
I = rgb2gray(I);

% Initialize A
[row, col] = size(I);
A = uint8(zeros(row, col));

% Initialize the structuring element shape and size
se = strel('square',5);

% Loop until kmax and lmax.
kmax = row/2; 
lmax = col/2;

for i = 1:1:kmax
    disp(i);
    for j = 1:1:lmax
        % Compute S from image I by using circular shift by rk, l).
        S = imCshift(I, i, j);
        % Compute binary image D =|I ? S|< t
        D = simThresh(I,S,1);

        % Whenever required, use MATLAB inbuilt (imdilate) and (imerode)
        % Use Erosion followed by Dilation
        % You are free to choose any structuring element shape and size 
        % that gives you the best result. State your choices in the report.

        % Erode and dilate D using a structure of size b x b to create Ded.
        erodedI = imerode(D,se);
    %     imshow(erodedI, []);
        dilatedI = imdilate(erodedI,se);

        % Update: A = A OR Ded.
        A = (A | dilatedI);
    end
end

[row, col] = size(A);
for i = 1:1:row
    for j = 1:1:col
        if A(i,j) == 1
            I(i,j) = 255;
        end
    end
end

figure,imshow(I, []);
title('Result of detecting tempered region');
saveas(gcf,'lab5_1_6_result_region.pdf');








