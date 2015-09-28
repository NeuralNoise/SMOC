{
TASK: mod
LANG: PASCAL
}

var
    s,u,n,r:integer;
begin
    readln(s,u);
    r := 1;
    repeat
        n := (s + u) >> 1 ;
        writeln(n);
        readln(r);
        case r of
            -1 : u := n - 1;
             1 : s := n + 1;
        end;
    until (r = 0);
end.
