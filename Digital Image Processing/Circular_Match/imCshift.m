function S = imCshift(X,k,l)
% X - the image to be shifted
% k - the number of pixels to shift row-wise
% l - the number of pixels to shift column-wise
% The function should return a shifted image, S.
[row, col] = size(X);

% Initialize the shifted image S
S = uint8(zeros(row,col));
% Shift the image to the right
temp = X(:,col+1-k:col);
S(:,k+1:col) = X(:,1:col-k);
S(:,1:k) = temp;

temp = S(row+1-l:row, :);
S(l+1:row,:) = S(1:row-l,:);
S(1:l,:) = temp;

end