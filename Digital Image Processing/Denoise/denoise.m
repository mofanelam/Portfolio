% Read the image halftone_evidence.pgm
I = imread('halftone_evidence.pgm');
% Add padding to the image.
PQ = paddedsize(size(I));
% Apply fourier transform.
F = fft2(I, PQ(1), PQ(2));
% Analyse the spike in terms of the fourier spectrum.
frequencyImage = fftshift(fft2(I, PQ(1), PQ(2)));
amplitudeImage = log(abs(frequencyImage));

D0 = 0.03*PQ(1);
D1 = 0.3*PQ(1);
D2 = 0.25*PQ(1);

% Apply an ideal filter to filter out most of the unwanted peaks.
H_ideal = lpfilter('ideal', PQ(1), PQ(2), D2);
% Apply gaussian filters on hard-coded locations.
H1 = notch('gaussian', PQ(1), PQ(2), D0, 513, 257);
H1 = fftshift(H1);
H2 = notch('gaussian', PQ(1), PQ(2), D0, 257, 513);
H2 = fftshift(H2);
H3 = notch('gaussian', PQ(1), PQ(2), D0, 770, 513);
H3 = fftshift(H3);
H4 = notch('gaussian', PQ(1), PQ(2), D0, 513, 770);
H4 = fftshift(H4);
% Apply the masks
G = F  .* H_b .* H_ideal.* H1 .* H2 .* H3 .* H4;
g = real(ifft2(G));
% Analyse the filtered fourier spectrum.
amplitudeImage2 = log(abs(fftshift(G)));
% Remove the paddings.
g = g(1:size(I,1), 1:size(I,2));
g = uint8(g);





