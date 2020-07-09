function diffImages = jpeg_ghosts(file, b, minQ, maxQ, stepQ)
    [row, col, dim] = size(file);
    pad = b - 1;

    total = ceil((maxQ-minQ)/stepQ);
    spat_averaged_image = zeros(row,col,total);

    pad_r_file = double(padarray(file(:,:,1),[pad pad],0,'post'));
    pad_g_file = double(padarray(file(:,:,2),[pad pad],0,'post'));
    pad_b_file = double(padarray(file(:,:,3),[pad pad],0,'post'));

    for i = minQ:stepQ:maxQ
        count = ceil(i/stepQ);
        disp(i);
        imwrite(file, 'temp.jpg', 'jpeg', 'Quality', i);
        temp = imread('temp.jpg');
        % Getting rgb for comparing image
        pad_r_temp = double(padarray(temp(:,:,1),[pad pad],0,'post'));
        pad_g_temp = double(padarray(temp(:,:,2),[pad pad],0,'post'));
        pad_b_temp = double(padarray(temp(:,:,3),[pad pad],0,'post'));
        
        r_summation = double(0);
        g_summation = double(0);
        b_summation = double(0);

        for r = 1:1:row
            for c = 1:1:col
                    for j = 0:1:4
                        for k = 0:1:4
                            loc_r = (r + j);
                            loc_c = (c + k);
                            temp_r = ((pad_r_temp(loc_r,loc_c) - pad_r_file(loc_r,loc_c))^2 / b^2);
                            r_summation = r_summation + temp_r;
                        end
                    end
                    for j = 0:1:4
                        for k = 0:1:4
                            loc_r = (r + j);
                            loc_c = (c + k);
                            temp_g = ((pad_g_temp(loc_r,loc_c) - pad_g_file(loc_r,loc_c))^2 / b^2);
                            g_summation = g_summation + temp_g;
                        end
                    end
                    for j = 0:1:4
                        for k = 0:1:4
                            loc_r = (r + j);
                            loc_c = (c + k);
                            temp_b = ((pad_b_temp(loc_r,loc_c) - pad_b_file(loc_r,loc_c))^2 / b^2);
                            b_summation = b_summation + temp_b;
                        end
                    end
                    r_summation = r_summation / b^2;
                    g_summation = g_summation / b^2;
                    b_summation = b_summation / b^2;
                    sum = (r_summation + g_summation + b_summation) / 3;
                    spat_averaged_image(r,c,count) = sum;
            end
        end
    end

    % Normalize the image
    normalizedImg = zeros(row, col,total);

    for r = 1:1:row
        disp(r);
        for c = 1:1:col
            temp_arr = zeros(total,1);
            for i = 1:1:total
                temp_arr(i) = spat_averaged_image(r,c,i);
            end
            min_val = min(temp_arr);
            max_val = max(temp_arr);
            for i = 1:1:total
               normalizedImg(r,c,i) = (spat_averaged_image(r,c,i) - min_val) /  (max_val - min_val);
            end
        end
    end

    diffImages = normalizedImg;
end
    











