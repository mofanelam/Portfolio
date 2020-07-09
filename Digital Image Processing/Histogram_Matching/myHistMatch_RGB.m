% function [enhancedImage] = myHistMatch(inputImage, refImage)
    % Read both the input Image and the reference image
    I = imread('color_cast.png');
    Ref_image = imread('hist_ref.png');

    % Separate the RGB channels from I.
    R = I(:,:,1);
    G = I(:,:,2);
    B = I(:,:,3);

    % Find out the sizes of input and reference image
    image_size_ref = size(Ref_image);
    image_size_RGB = size(R);

    % Initialize enhanced image
    enhancedImage = zeros(image_size_RGB(1,1), image_size_RGB(1,2), 3, 'uint8');

    % Find the total pixel of the image

    tp_ref = image_size_ref(1,1) * image_size_ref(1,2);
    ref_normalized = zeros(1,256);

    % Precalculate the reference constant:
    ref_constant = 1 / tp_ref;

    % Calculate the normalized reference image
    for i = 1:1:image_size_ref(1,1)
        for j = 1:1:image_size_ref(1,2)
            temp = Ref_image(i,j);
                ref_normalized(1,temp+1) = ref_normalized(1,temp+1) + ref_constant;
        end
    end

    % Compute on RGB
    tp_RGB = image_size_RGB(1,1) * image_size_RGB(1,2);
    ref_R = zeros(1,256);
    ref_G = zeros(1,256);
    ref_B = zeros(1,256);

    % Precalculate the constant:
    RGB_constant = 1 / tp_RGB;

    % For R channel
    for i = 1:1:image_size_RGB(1,1)
        for j = 1:1:image_size_RGB(1,2)
            temp = R(i,j);
                ref_R(1,temp+1) = ref_R(1,temp+1) + RGB_constant;
        end
    end

    % For G channel
    for i = 1:1:image_size_RGB(1,1)
        for j = 1:1:image_size_RGB(1,2)
            temp = G(i,j);
                ref_G(1,temp+1) = ref_G(1,temp+1) + RGB_constant;
        end
    end

    % For B channel
    for i = 1:1:image_size_RGB(1,1)
        for j = 1:1:image_size_RGB(1,2)
            temp = B(i,j);
                ref_B(1,temp+1) = ref_B(1,temp+1) + RGB_constant;
        end
    end

    % Initiate CDF for ref and R G B
    cdf_ref = zeros(1,256);
    cdf_R = zeros(1,256);
    cdf_G = zeros(1,256);
    cdf_B = zeros(1,256);
    temp2 = 0.0;

    % Calculate CDF for reference
    for i = 1:1:256
       temp2 = temp2 + ref_normalized(1,i);
       cdf_ref(1,i) = temp2;
    end

    % Calculate CDF for RGB
    temp2 = 0.0;
    for i = 1:1:256
       temp2 = temp2 + ref_R(1,i);
       cdf_R(1,i) = temp2;
    end

    temp2 = 0.0;
    for i = 1:1:256
       temp2 = temp2 + ref_G(1,i);
       cdf_G(1,i) = temp2;
    end

    temp2 = 0.0;
    for i = 1:1:256
       temp2 = temp2 + ref_B(1,i);
       cdf_B(1,i) = temp2;
    end

    M = zeros(1, 256);

    % Mapping between R and ref.
    for i = 1 : 256
        [~,ind] = min(abs(cdf_R(i) - cdf_ref));
        M(i) = ind-1;
    end
    enhanced_R = uint8(M(double(R)+1));
    enhancedImage(:,:,1) = enhanced_R;

    % Mapping between G and ref.
    for i = 1 : 256
        [~,ind] = min(abs(cdf_G(i) - cdf_ref));
        M(i) = ind-1;
    end
    enhanced_G = uint8(M(double(G)+1));
    enhancedImage(:,:,2) = enhanced_G;

    % Mapping between B and ref.
    for i = 1 : 256
        [~,ind] = min(abs(cdf_B(i) - cdf_ref));
        M(i) = ind-1;
    end
    enhanced_B = uint8(M(double(B)+1));
    enhancedImage(:,:,3) = enhanced_B;
    
%     figure
%     subplot(2,3,1);
%     imhist(R);
%     subplot(2,3,2);
%     imhist(G);
%     subplot(2,3,3);
%     imhist(B);
%     subplot(2,3,4);
%     imhist(enhanced_R);
%     subplot(2,3,5);
%     imhist(enhanced_G);
%     subplot(2,3,6);
%     imhist(enhanced_B);
% histogram(R,'Normalization','probability')
% print('normalized_R','-fillpage','-dpdf');
% histogram(G,'Normalization','probability')
% print('normalized_G','-fillpage','-dpdf');
% histogram(B,'Normalization','probability')
% print('normalized_B','-fillpage','-dpdf');
% histogram(enhanced_R,'Normalization','probability')
% print('normalized_enhanced_R','-fillpage','-dpdf');
% histogram(enhanced_G,'Normalization','probability')
% print('normalized_enhanced_G','-fillpage','-dpdf');
% histogram(enhanced_B,'Normalization','probability')
% print('normalized_enhanced_B','-fillpage','-dpdf');


