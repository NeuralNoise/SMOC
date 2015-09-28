unit unbuffered_stdout;
interface
implementation
uses
  dos;
initialization
  TextRec(Output).FlushFunc := TextRec(Output).InOutFunc;
end.
