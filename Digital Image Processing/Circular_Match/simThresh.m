function d = simThresh(x, s, t)

% x - The original image
% s - The shifted image
% t - A threshold
x = double(x);
s = double(s);
temp = abs(x-s);
% The threshold
d = uint8(temp < t);

end